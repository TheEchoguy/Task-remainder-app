package com.tracker.app.repository;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Integer> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByPriority(TaskPriority priority,Pageable pageable);
    List<Task> findByTitle(String keyword);
    List<Task> findByDueDate(String date);
    Page<Task> findByTitleContainingIgnoreCase(String keyword,Pageable pageable);
    Page<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority,Pageable pageable);


}
