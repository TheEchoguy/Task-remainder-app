package com.tracker.app.controller;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping()
    public String listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String keyword,

//            @RequestParam(required = false) String sort,
            Model model) {

        List<Task> tasks = null;
        Pageable pageable= PageRequest.of(page,size);
        Page<Task> taskPage=taskService.getPagedTasks(pageable,status,priority,keyword);
        model.addAttribute("taskPage",taskPage);
        model.addAttribute("tasks",taskPage.getContent());
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",taskPage.getTotalPages());
        model.addAttribute("size",size);
        int totalPages=taskPage.getTotalPages();
        List<Integer> pageNumbers= IntStream.range(0,totalPages).boxed().toList();
        model.addAttribute("pageNumbers",pageNumbers);

        return "tasks";
    }

    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("task",new Task());
        return "add-task";
    }
    @PostMapping("/add")
    public String saveTask(@ModelAttribute Task task , Model model , RedirectAttributes ra){
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Title is required!");
            model.addAttribute("task", task);
            return "add-task";
        }

        if (task.getDueDate() == null || task.getDueDate().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Due Date is required!");
            model.addAttribute("task", task);
            return "add-task";
        }
        if (task.getStatus()==null){
            task.setStatus(TaskStatus.PENDING);
        }

        task.setCreatedAt(LocalDateTime.now());
        taskService.addTask(task);
        ra.addFlashAttribute("successMessage","Task added successfully!!");
        return "redirect:/api/tasks";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Task task = taskService.findById(id).orElseThrow(()->new RuntimeException("Task not found"));

        model.addAttribute("task", task);
        return "edit-task";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Integer id, @ModelAttribute Task task, Model model, RedirectAttributes ra) {

        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Title is required!");
            model.addAttribute("task", task);
            return "edit-task";
        }

        if (task.getDueDate() == null || task.getDueDate().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Due Date is required!");
            model.addAttribute("task", task);
            return "edit-task";
        }

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        Task existing = taskService.findById(id).orElse(null);
        if (existing == null) {
            model.addAttribute("errorMessage", "Task not found");
            return "edit-task";
        }

        task.setId(id);
        task.setCreatedAt(existing.getCreatedAt());

        // 🔹 STATUS → completedAt logic
        if (task.getStatus() == TaskStatus.DONE) {
            // First time marking DONE
            if (existing.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(existing.getCompletedAt()); // keep original
            }
        } else {
            task.setCompletedAt(null);
        }

        // 🔹 Save update
        taskService.updateTask(task);

        ra.addFlashAttribute("successMessage", "Task updated successfully!");
        return "redirect:/api/tasks";
    }


    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Integer id,Model model)
    {
        if(!taskService.findById(id).isPresent()){
            model.addAttribute("errorMessage","Task not found");
            return "Tasks";
        }
        taskService.deleteTask(id);
        return "redirect:/api/tasks";
    }
    @GetMapping("/markdone/{id}")
    public String markAsDone(@PathVariable Integer id) {
        Task task = taskService.findById(id).orElse(null);

        if (task != null && task.getStatus()!=TaskStatus.DONE) {
            task.setStatus(TaskStatus.DONE);
            task.setCompletedAt(LocalDateTime.now());
            taskService.updateTask(task);
        }

        return "redirect:/api/tasks";
    }
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Task task = taskService.findById(id).orElse(null);
        if (task == null) {
            ra.addFlashAttribute("errorMessage", "Task not found!");
            return "redirect:/api/tasks";
        }
        model.addAttribute("task",task);
        return "view-task";

    }

}
//localhost/api/tasks,add,update,delete