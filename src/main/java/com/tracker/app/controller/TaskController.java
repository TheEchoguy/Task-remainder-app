package com.tracker.app.controller;

import com.tracker.app.entity.Task;
import com.tracker.app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String listTasks(Model model){
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks";
    }
    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("task",new Task());
        return "add-task";
    }
    @PostMapping("/add")
    public String saveTask(@ModelAttribute Task task , Model model){
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

        task.setCreatedAt(LocalDateTime.now());
        taskService.addTask(task);
        return "redirect:/api/tasks";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Task task = taskService.findById(id).orElseThrow(()->new RuntimeException("Task not found"));

        model.addAttribute("task", task);
        return "edit-task";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Integer id, @ModelAttribute Task task, Model model) {
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
        Task existing=taskService.findById(id).orElse(null);
        if (existing==null){
            model.addAttribute("errorMessage","Task not found");
        }
        task.setCreatedAt(existing.getCreatedAt());
        taskService.updateTask(task);
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


}
//localhost/api/tasks,add,update,delete