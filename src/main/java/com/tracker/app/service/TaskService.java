package com.tracker.app.service;
import java.util.List;
import java.util.Optional;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
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
}
