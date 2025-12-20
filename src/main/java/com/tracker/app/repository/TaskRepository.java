package com.tracker.app.repository;

import com.tracker.app.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Integer> {
    List<Task> findByStatus(String status);
    List<Task> findByPriority(String priority);
    List<Task> findByTitle(String keyword);
    List<Task> findByDueDate(String date);
    List<Task> findByTitleContainingIgnoreCase(String keyword);

}
