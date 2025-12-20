package com.tracker.app.controller;

import com.tracker.app.entity.Task;
import com.tracker.app.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping()
    public String listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            Model model) {

        List<Task> tasks = null;

        if (status != null && !status.isEmpty()) {
            tasks = taskService.findByStatus(status);

        } else if (priority != null && !priority.isEmpty()) {
            tasks = taskService.findByPriority(priority);

        } else if (keyword != null && !keyword.isEmpty()) {
            tasks = taskService.searchByTitle(keyword);

        } else if (sort != null && !sort.isEmpty()) {
           // tasks = taskService.sortByField(sort);

        } else {
            tasks = taskService.getAllTasks();
        }

        model.addAttribute("tasks", tasks);
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
        Task existing=taskService.findById(id).orElse(null);
        if (existing==null){
            model.addAttribute("errorMessage","Task not found");
        }
        task.setCreatedAt(existing.getCreatedAt());
        taskService.updateTask(task);
        ra.addFlashAttribute("successMessage","Task updated successfully !!");
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

        if (task != null) {
            task.setStatus("Done");
            taskService.updateTask(task);
        }

        return "redirect:/api/tasks";
    }
    @GetMapping("/markpending/{id}")
    public String markAsPending(@PathVariable Integer id) {
        Task task = taskService.findById(id).orElse(null);

        if (task != null) {
            task.setStatus("Pending");
            taskService.updateTask(task);
        }

        return "redirect:/api/tasks";
    }



}
//localhost/api/tasks,add,update,delete