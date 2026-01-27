package com.tracker.app.controller;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Task> tasks = taskService.getTasksByUser(userId);

        long total = tasks.size();
        long completed = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        long pending = total - completed;

        long overdue = tasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        LocalDate.parse(t.getDueDate()).isBefore(LocalDate.now()) &&
                        t.getStatus() != TaskStatus.DONE)
                .count();



        long dueToday = tasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        LocalDate.parse(t.getDueDate()).isEqual(LocalDate.now()))
                .count();



        long highPriority = tasks.stream()
                .filter(t -> t.getPriority() == TaskPriority.HIGH)
                .count();

        List<Task> recentTasks = tasks.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("total", total);
        model.addAttribute("completed", completed);
        model.addAttribute("pending", pending);
        model.addAttribute("overdue", overdue);
        model.addAttribute("dueToday", dueToday);
        model.addAttribute("highPriority", highPriority);
        model.addAttribute("recentTasks", recentTasks);

        return "dashboard";
    }
}
