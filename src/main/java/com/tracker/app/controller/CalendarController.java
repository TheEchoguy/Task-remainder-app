package com.tracker.app.controller;

import com.tracker.app.entity.Task;
import com.tracker.app.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CalendarController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/calendar")
    public String calendar(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Task> tasks = taskService.getTasksByUser(userId);

        Map<String, List<Map<String, String>>> tasksByDate =
                tasks.stream()
                        .filter(t -> t.getDueDate() != null)
                        .collect(Collectors.groupingBy(
                                t -> t.getDueDate(),   // since dueDate is String
                                Collectors.mapping(t -> Map.of(
                                        "title", t.getTitle(),
                                        "priority", t.getPriority().name(),
                                        "status", t.getStatus().name()   // ✅ IMPORTANT
                                ), Collectors.toList())
                        ));

        LocalDate today = LocalDate.now();
        model.addAttribute("year", today.getYear());
        model.addAttribute("month", today.getMonthValue());
        model.addAttribute("tasksByDate", tasksByDate);

        return "calendar";
    }



}
