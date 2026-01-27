package com.tracker.app.entity;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer id;
    private String title;
    private String description;
    private String dueDate;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;
    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;


    public Task(){

    }

    public Task(Integer id, String title, String description, String dueDate, TaskStatus status , TaskPriority priority, LocalDateTime createdAt, LocalDateTime completedAt){
        this.id= id;
        this.title=title;
        this.description=description;
        this.dueDate=dueDate;
        this.status=status;
        this.priority=priority;
        this.createdAt=createdAt;
        this.completedAt=completedAt;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String  dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt;}

    public User getUser(){ return user;}

    public User setUser(User user){ return this.user= user;}

    public Boolean getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

}

