package com.tracker.app.service;
import java.util.List;
import java.util.Optional;

import com.tracker.app.entity.Task;
import com.tracker.app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
