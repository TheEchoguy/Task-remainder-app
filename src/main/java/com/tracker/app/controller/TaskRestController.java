package com.tracker.app.controller;


import com.tracker.app.dto.OtpRequest;
import com.tracker.app.entity.Task;
import com.tracker.app.entity.User;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.service.TaskService;

import com.tracker.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskRestController {

    @Autowired
    private final TaskService taskService;
    private final UserService userService;


    @Autowired
    public TaskRestController(TaskService taskService, UserService userService){
    this.taskService = taskService;
    this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getAll(Pageable pageable){
        Page<Task> page= taskService.findAll(pageable);
        return ResponseEntity.ok(page);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<Task> t = taskService.findById(id);

        if (t.isPresent()) {
            return new ResponseEntity<>(t.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Task not present with "+id, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Task> create(@RequestBody Task task,Integer userId) {
        task.setCreatedAt(LocalDateTime.now());
        Task saved = taskService.addTask(task,userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Integer id, @RequestBody Task task,Integer userId) {
        Optional<Task> existing = taskService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        if (task.getCreatedAt() == null) {
            task.setCreatedAt(existing.get().getCreatedAt());
        }

        task.setId(id);
        Task updated = taskService.updateTask(id,task,userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (taskService.findById(id).isEmpty()) return ResponseEntity.notFound().build();

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

   @GetMapping("/status/{status}")
    public ResponseEntity<Page<Task>> getByStatus(@PathVariable TaskStatus status,Pageable pageable) {
        return ResponseEntity.ok(taskService.findByStatus(status,pageable));
    }

   @GetMapping("/priority/{priority}")
    public ResponseEntity<Page<Task>> getByPriority(@PathVariable TaskPriority priority,Pageable pageable) {
        return ResponseEntity.ok(taskService.findByPriority(priority,pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Task>> searchByTitle(@RequestParam("keyword") String keyword,Pageable pageable) {
        return ResponseEntity.ok(taskService.searchByTitle(keyword,pageable));
    }

    @GetMapping("/due")
    public ResponseEntity<List<Task>> getByDueDate(@RequestParam("date") String date) {
        return ResponseEntity.ok(taskService.findByDueDate(date));
    }

    @GetMapping("/due-today")
    public ResponseEntity<List<Task>> getTasksDueToday() {
        return ResponseEntity.ok(taskService.getTasksDueToday());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Task>> getUpcomingTasks(
            @RequestParam(defaultValue = "3") int days) {

        return ResponseEntity.ok(taskService.getUpcomingTasks(days));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        return ResponseEntity.ok(taskService.getOverdueTasks());
    }

    @PostMapping("/userRegister")
    public ResponseEntity<?> registerApi(@RequestBody User user) {

        try {
            userService.register(user);
            return ResponseEntity.ok("OTP sent to your email");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody OtpRequest request,
                                    HttpSession session) {

        String email = (String) session.getAttribute("otpEmail");

        if (email == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Session expired. Please register again.");
        }

        Boolean resetFlow = (Boolean) session.getAttribute("resetFlow");
        boolean isReset = Boolean.TRUE.equals(resetFlow);

        String response = userService.verifyOtp(
                email,
                request.getOtp(),
                isReset
        );

        if ("Email verified successfully".equals(response)) {
            session.removeAttribute("otpEmail");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }
    @PostMapping("/resendOtp")
    public ResponseEntity<?> resendOtp(@RequestBody OtpRequest request) {

        String response = userService.resendOtp(request.getEmail());

        return ResponseEntity.ok(response);
    }


}
