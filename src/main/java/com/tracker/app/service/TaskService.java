package com.tracker.app.service;
import java.util.List;
import java.util.Optional;

import com.tracker.app.entity.Task;
import com.tracker.app.repository.TaskRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }
    public Task addTask(Task task){
        return taskRepository.save(task);
    }
    public Optional<Task> findById(Integer id){
        return taskRepository.findById(id);
    }
    public Task updateTask(Task task){
       return taskRepository.save(task);
    }
    public void deleteTask(Integer id){
        taskRepository.deleteById(id);

    }

    public List<Task> findByStatus(String status) {
        return taskRepository.findByStatus(status);
    }


    public List<Task> findByPriority(String priority) {
        return taskRepository.findByPriority(priority);
    }

    public List<Task> searchByTitle(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword);
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
}
