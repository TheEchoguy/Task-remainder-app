package com.tracker.app.repository;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
    Page<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    List<Task> findByTitle(String keyword);

    List<Task> findByDueDate(String date);
    List<Task> findByDueDateBetween(String start, String end);
    List<Task> findByDueDateBefore(String date);
    List<Task> findByUserId(Integer userId);




    Page<Task> findByUserId(Integer userId, Pageable pageable);
    Page<Task> findByUserIdAndStatus(Integer userId, TaskStatus status, Pageable pageable);
    Page<Task> findByUserIdAndPriority(Integer userId, TaskPriority priority, Pageable pageable);
    Page<Task> findByUserIdAndTitleContainingIgnoreCase(Integer userId, String keyword, Pageable pageable);


    long countByUserId(Integer userId);
    long countByUserIdAndStatus(Integer userId, TaskStatus status);

    List<Task> findByReminderSentFalseAndReminderTimeBefore(LocalDateTime now);
    List<Task> findByReminderSentFalseAndReminderTimeBeforeAndStatusNot(
            LocalDateTime now,
            TaskStatus status
    );


    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.user.id = :userId
        AND t.status <> 'DONE'
    """)
    long countPendingTasks(@Param("userId") Integer userId);

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.user.id = :userId
        AND t.status <> 'DONE'
        AND t.dueDate < :now
    """)
    long countOverdueTasks(
            @Param("userId") Integer userId,
            @Param("now") LocalDateTime now
    );
    @Query("""
    SELECT t FROM Task t
    WHERE t.reminderTime IS NOT NULL
    AND t.reminderSent = false
    AND t.status <> com.tracker.app.enums.TaskStatus.DONE
    AND t.reminderTime <= :now
    """)
    List<Task> findPendingReminders(LocalDateTime now);

}
