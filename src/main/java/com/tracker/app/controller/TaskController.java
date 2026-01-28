package com.tracker.app.controller;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.service.EmailService;
import com.tracker.app.service.TaskService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private EmailService emailService;


    @GetMapping()
    public String listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            Model model,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Pageable pageable;
        if ("dueDate".equals(sort)) {
            pageable = PageRequest.of(page, size,
                    org.springframework.data.domain.Sort.by("dueDate"));
        } else if ("priority".equals(sort)) {
            pageable = PageRequest.of(page, size,
                    org.springframework.data.domain.Sort.by("priority"));
        } else if ("title".equals(sort)) {
            pageable = PageRequest.of(page, size,
                    org.springframework.data.domain.Sort.by("title"));
        } else {
            pageable = PageRequest.of(page, size);
        }


        Page<Task> taskPage = taskService.getPagedTasksForUser(
                userId, pageable, status, priority, keyword
        );


        long totalTasks = taskService.countByUser(userId);
        long pendingTasks = taskService.countByUserAndStatus(userId, TaskStatus.PENDING);
        long completedTasks = taskService.countByUserAndStatus(userId, TaskStatus.DONE);

        int progress = totalTasks == 0
                ? 0
                : (int) ((completedTasks * 100) / totalTasks);


        model.addAttribute("taskPage", taskPage);
        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("progress", progress);

        List<Integer> pageNumbers =
                IntStream.range(0, taskPage.getTotalPages()).boxed().toList();
        model.addAttribute("pageNumbers", pageNumbers);

        return "tasks";
    }


    @GetMapping("/add")
    public String showAddForm(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("task", new Task());
        return "add-task";
    }

    @PostMapping("/add")
    public String saveTask(@ModelAttribute Task task, Model model, RedirectAttributes ra, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
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
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.LOW);
        }
        // Reset reminder flag if reminder time exists
        if (task.getReminderTime() != null) {
            task.setReminderSent(false);
        }


        task.setCreatedAt(LocalDateTime.now());
        taskService.addTask(task, userId);
        ra.addFlashAttribute("successMessage", "Task added successfully!!");
        return "redirect:/api/tasks";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Task task = taskService.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        model.addAttribute("task", task);
        return "edit-task";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Integer id, @ModelAttribute Task task, Model model, RedirectAttributes ra, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
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
        if (task.getReminderTime() != null &&
                (existing.getReminderTime() == null ||
                        !task.getReminderTime().equals(existing.getReminderTime()))) {

            task.setReminderSent(false);
        }

        task.setId(id);
        task.setCreatedAt(existing.getCreatedAt());

        if (task.getStatus() == TaskStatus.DONE) {
            if (existing.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(existing.getCompletedAt()); // keep original
            }
        } else {
            task.setCompletedAt(null);
        }
        taskService.updateTask(id, task, userId);

        ra.addFlashAttribute("successMessage", "Task updated successfully!");
        return "redirect:/api/tasks";
    }


    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Integer id, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        if (!taskService.findById(id).isPresent()) {
            model.addAttribute("errorMessage", "Task not found");
            return "Tasks";
        }
        taskService.deleteTask(id);
        return "redirect:/api/tasks";
    }

    @GetMapping("/markdone/{id}")
    public String markAsDone(@PathVariable Integer id, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Task task = taskService.findById(id).orElse(null);

        if (task != null && task.getStatus() != TaskStatus.DONE) {

            task.setStatus(TaskStatus.DONE);
            task.setCompletedAt(LocalDateTime.now());

            // IMPORTANT: disable reminders
            task.setReminderTime(null);
            task.setReminderSent(true); // prevents scheduler pickup

            taskService.updateTask(id, task, userId);
        }

        return "redirect:/api/tasks";
    }


    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model, RedirectAttributes ra, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Task task = taskService.findById(id).orElse(null);
        if (task == null) {
            ra.addFlashAttribute("errorMessage", "Task not found!");
            return "redirect:/api/tasks";
        }
        model.addAttribute("task", task);
        return "view-task";

    }

    @GetMapping("/export/download")
    public void downloadCsv(HttpServletResponse response, HttpSession session)
            throws IOException {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect("/login");
            return;
        }

        List<Task> tasks = taskService.getTasksByUser(userId);

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=tasks.csv"
        );

        PrintWriter writer = response.getWriter();
        writer.println("ID,Title,Description,Status,Priority,Due Date,Created At,Completed At");

        for (Task task : tasks) {
            writer.println(
                    task.getId() + "," +
                            escape(task.getTitle()) + "," +
                            escape(task.getDescription()) + "," +
                            task.getStatus() + "," +
                            task.getPriority() + "," +
                            task.getDueDate() + "," +
                            task.getCreatedAt() + "," +
                            task.getCompletedAt()
            );
        }

        writer.flush();
    }
    @GetMapping("/export/email")
    public String emailCsv(
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Task> tasks = taskService.getTasksByUser(userId);

        if (tasks.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "toastError",
                    "No tasks available to email"
            );
            return "redirect:/api/tasks";
        }

        String userEmail = tasks.get(0).getUser().getEmail();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Description,Status,Priority,Due Date,Created At,Completed At\n");

        for (Task task : tasks) {
            csv.append(task.getId()).append(",")
                    .append(escape(task.getTitle())).append(",")
                    .append(escape(task.getDescription())).append(",")
                    .append(task.getStatus()).append(",")
                    .append(task.getPriority()).append(",")
                    .append(task.getDueDate()).append(",")
                    .append(task.getCreatedAt()).append(",")
                    .append(task.getCompletedAt())
                    .append("\n");
        }

        emailService.sendTasksCsv(
                userEmail,
                csv.toString().getBytes()
        );

        redirectAttributes.addFlashAttribute(
                "toastSuccess",
                "CSV sent to your email successfully 📧"
        );

        return "redirect:/api/tasks";
    }

    private String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }
    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail() {
        emailService.sendOTP("skus0426@gmail.com", "123456");
        return "Email sent";
    }

}




