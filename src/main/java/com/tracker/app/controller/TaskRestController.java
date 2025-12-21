package com.tracker.app.controller;


import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.service.TaskService;

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

    @Autowired
    public TaskRestController(TaskService taskService){
    this.taskService = taskService;
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
    public ResponseEntity<Task> create(@RequestBody Task task) {
        task.setCreatedAt(LocalDateTime.now());
        Task saved = taskService.addTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Integer id, @RequestBody Task task) {
        Optional<Task> existing = taskService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        if (task.getCreatedAt() == null) {
            task.setCreatedAt(existing.get().getCreatedAt());
        }

        task.setId(id);
        Task updated = taskService.updateTask(task);
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
}
