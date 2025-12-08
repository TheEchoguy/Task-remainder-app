package com.tracker.app.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tracker.app.entity.Task;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final List<Task> tasks= new ArrayList<>();
    private static int counter=100;

    public TaskService(){
        tasks.add(new Task(1,"Learn Spring Boot","Basics for project",
                "2025-01-01","IN_PROGRESS", "HIGH",
                LocalDateTime.now().minusDays(10)));
        tasks.add(new Task(2, "Build REST API", "Create CRUD endpoints",
                "2025-01-04", "IN_PROGRESS", "HIGH",
                LocalDateTime.now().minusDays(10)));

        tasks.add(new Task(3, "Learn Thymeleaf", "Implement UI templates",
                "2025-01-08", "PENDING", "MEDIUM",
                LocalDateTime.now().minusDays(9)));

        tasks.add(new Task(4, "Implement Service Layer", "Add business logic methods",
                "2025-01-12", "COMPLETED", "HIGH",
                LocalDateTime.now().minusDays(8)));

        tasks.add(new Task(5, "Add Task Validation", "Validate user inputs",
                "2025-01-15", "IN_PROGRESS", "LOW",
                LocalDateTime.now().minusDays(7)));

        tasks.add(new Task(6, "Connect to Database", "Use MySQL + JPA",
                "2025-01-18", "PENDING", "HIGH",
                LocalDateTime.now().minusDays(6)));

        tasks.add(new Task(7, "Add Priority Feature", "High/Medium/Low options",
                "2025-01-20", "COMPLETED", "MEDIUM",
                LocalDateTime.now().minusDays(5)));

        tasks.add(new Task(8, "Implement Login System", "Use Spring Security",
                "2025-01-25", "PENDING", "HIGH",
                LocalDateTime.now().minusDays(4)));

        tasks.add(new Task(9, "UI Styling", "Improve UI with Bootstrap",
                "2025-01-28", "IN_PROGRESS", "LOW",
                LocalDateTime.now().minusDays(3)));

        tasks.add(new Task(10, "Reminder Email", "Send reminders via SMTP",
                "2025-02-01", "PENDING", "MEDIUM",
                LocalDateTime.now().minusDays(2)));

        tasks.add(new Task(11, "Deploy Application", "Deploy on Render/AWS",
                "2025-02-05", "IN_PROGRESS", "HIGH",
                LocalDateTime.now().minusDays(1)));

    }
    public List<Task> getAllTasks(){
        return tasks;
    }
    public void addTask(Task task){
        tasks.add(task);
    }
    public static int nextId(){
        return counter++;
    }
}
