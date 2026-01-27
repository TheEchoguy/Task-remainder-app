package com.tracker.app.service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tracker.app.entity.Task;
import com.tracker.app.entity.User;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.repository.TaskRepository;
import com.tracker.app.repository.UserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }
    public Task addTask(Task task,Integer userId){
        Optional<User> user = userRepository.findById(userId);
        task.setUser(user.get());

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        task.setCreatedAt(LocalDateTime.now());

        return taskRepository.save(task);

    }
    public Optional<Task> findById(Integer id){
        return taskRepository.findById(id);
    }
    public Task updateTask(Integer taskId, Task task, Integer userId) {

        Task existing = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // 🔐 Security check
        if (!existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDueDate(task.getDueDate());
        existing.setPriority(task.getPriority());
        existing.setStatus(task.getStatus());

        // Reminder handling
        if (task.getReminderTime() != null &&
                (existing.getReminderTime() == null ||
                        !task.getReminderTime().equals(existing.getReminderTime()))) {

            existing.setReminderTime(task.getReminderTime());
            existing.setReminderSent(false);
        }

        // Completion logic
        if (existing.getStatus() == TaskStatus.DONE) {
            if (existing.getCompletedAt() == null) {
                existing.setCompletedAt(LocalDateTime.now());
            }
        } else {
            existing.setCompletedAt(null);
        }

        return taskRepository.save(existing);
    }


    public void deleteTask(Integer id){
        taskRepository.deleteById(id);

    }
    public Page<Task> findByStatusAndPriority(TaskStatus status,TaskPriority priority,Pageable pageable){
        return taskRepository.findByStatusAndPriority(status,priority,pageable);
    }

    public Page<Task> findByStatus(TaskStatus status,Pageable pageable) {
        return taskRepository.findByStatus(status,pageable);
    }
    public Page<Task> getPagedTasks(Pageable pageable,TaskStatus status,TaskPriority priority,String keyword){
        if(status!= null && priority!= null){
            return taskRepository.findByStatusAndPriority(status,priority,pageable);
        } else if (status!=null) {
            return taskRepository.findByStatus(status,pageable);
        } else if (priority!=null) {
            return taskRepository.findByPriority(priority,pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return taskRepository.findByTitleContainingIgnoreCase(keyword,pageable);
        }
        return taskRepository.findAll(pageable);
    }


    public Page<Task> findByPriority(TaskPriority priority,Pageable pageable) {
        return taskRepository.findByPriority(priority,pageable);
    }

    public Page<Task> searchByTitle(String keyword,Pageable pageable) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword,pageable);
    }

    public List<Task> findByDueDate(String date) {
        return taskRepository.findByDueDate(date);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Page<Task> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    public List<Task> getTasksDueToday() {
        LocalDate today = LocalDate.now();
        return taskRepository.findByDueDate(today.toString());
    }

    public List<Task> getUpcomingTasks(int days) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(days);

        return taskRepository.findByDueDateBetween(today.toString(), end.toString());
    }
    public List<Task> getOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findByDueDateBefore(today.toString());
    }
    public Page<Task> getPagedTasksForUser(Integer userId, Pageable pageable, TaskStatus status, TaskPriority priority, String keyword) {

        if (status != null) {
            return taskRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (priority != null) {
            return taskRepository.findByUserIdAndPriority(userId, priority, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return taskRepository.findByUserIdAndTitleContainingIgnoreCase(
                    userId, keyword, pageable);
        }

        return taskRepository.findByUserId(userId, pageable);
    }
    public long countByUser(Integer userId) {
        return taskRepository.countByUserId(userId);
    }

    public long countByUserAndStatus(Integer userId, TaskStatus status) {
        return taskRepository.countByUserIdAndStatus(userId, status);
    }
    public long countAllTasks(Integer userId) {
        return taskRepository.countByUserId(userId);
    }

    public long countCompleted(Integer userId) {
        return taskRepository.countByUserIdAndStatus(userId, TaskStatus.DONE);
    }

    public long countPending(Integer userId) {
        return taskRepository.countByUserIdAndStatus(userId, TaskStatus.PENDING);
    }

    public long countOverdue(Integer userId) {
        return taskRepository.countOverdueTasks(userId, LocalDateTime.now());
    }

    public List<Task> getTasksByUser(Integer userId) {
        return taskRepository.findByUserId(userId);
    }



}
