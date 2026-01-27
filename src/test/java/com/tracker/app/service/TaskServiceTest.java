package com.tracker.app.service;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskPriority;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void testGetAllTasks() {
        List<Task> tasks = List.of(new Task(), new Task());
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testFindAll() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        List<Task> result = taskService.findAll();

        assertTrue(result.isEmpty());
        verify(taskRepository).findAll();
    }



    @Test
    void testAddTask() {
        Task task = new Task();
        when(taskRepository.save(task)).thenReturn(task);

        Task saved = taskService.addTask(task);

        assertNotNull(saved);
        verify(taskRepository,times(1)).save(task);
    }

    @Test
    void testFindById() {
        Task mocktask = new Task();
        mocktask.setId(1);
        when(taskRepository.findById(1)).thenReturn(Optional.of(mocktask));

        Optional<Task> result = taskService.findById(1);

        assertTrue(result.isPresent());
        verify(taskRepository).findById(1);
    }


    @Test
    void testUpdateTask() {
        Task task = new Task();
        when(taskRepository.save(task)).thenReturn(task);

        Task updated = taskService.updateTask(task);

        assertNotNull(updated);
        verify(taskRepository).save(task);
    }


    @Test
    void testDeleteTask() {
        doNothing().when(taskRepository).deleteById(1);

        taskService.deleteTask(1);

        verify(taskRepository).deleteById(1);
    }


    @Test
    void testFindByStatusAndPriority() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Task> page = new PageImpl<>(List.of(new Task()));

        when(taskRepository.findByStatusAndPriority(
                TaskStatus.PENDING, TaskPriority.HIGH, pageable))
                .thenReturn(page);

        Page<Task> result =
                taskService.findByStatusAndPriority(TaskStatus.PENDING, TaskPriority.HIGH, pageable);

        assertEquals(1, result.getContent().size());
        verify(taskRepository).findByStatusAndPriority(
                TaskStatus.PENDING, TaskPriority.HIGH, pageable);
    }

    @Test
    void testFindByStatus() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findByStatus(eq(TaskStatus.DONE), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Task> result = taskService.findByStatus(TaskStatus.DONE, pageable);

        assertNotNull(result);
        verify(taskRepository).findByStatus(TaskStatus.DONE, pageable);
    }

    @Test
    void testFindByPriority() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findByPriority(eq(TaskPriority.LOW), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Task> result = taskService.findByPriority(TaskPriority.LOW, pageable);

        assertNotNull(result);
        verify(taskRepository).findByPriority(TaskPriority.LOW, pageable);
    }

    @Test
    void testSearchByTitle() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findByTitleContainingIgnoreCase("test", pageable))
                .thenReturn(Page.empty());

        Page<Task> result = taskService.searchByTitle("test", pageable);

        assertNotNull(result);
        verify(taskRepository).findByTitleContainingIgnoreCase("test", pageable);
    }


    @Test
    void testGetPagedTasks_StatusAndPriority() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findByStatusAndPriority(any(), any(), any()))
                .thenReturn(Page.empty());

        Page<Task> result = taskService.getPagedTasks(
                pageable, TaskStatus.PENDING, TaskPriority.HIGH, null);

        assertNotNull(result);
        verify(taskRepository).findByStatusAndPriority(
                TaskStatus.PENDING, TaskPriority.HIGH, pageable);
    }

    @Test
    void testGetPagedTasks_StatusOnly() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findByStatus(any(), any()))
                .thenReturn(Page.empty());

        taskService.getPagedTasks(pageable, TaskStatus.PENDING, null, null);

        verify(taskRepository).findByStatus(TaskStatus.PENDING, pageable);
    }

    @Test
    void testGetPagedTasks_PriorityOnly() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findByPriority(any(), any()))
                .thenReturn(Page.empty());

        taskService.getPagedTasks(pageable, null, TaskPriority.HIGH, null);

        verify(taskRepository).findByPriority(TaskPriority.HIGH, pageable);
    }

    @Test
    void testGetPagedTasks_KeywordOnly() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findByTitleContainingIgnoreCase(any(), any()))
                .thenReturn(Page.empty());

        taskService.getPagedTasks(pageable, null, null, "task");

        verify(taskRepository).findByTitleContainingIgnoreCase("task", pageable);
    }

    @Test
    void testGetPagedTasks_NoFilters() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findAll(pageable)).thenReturn(Page.empty());

        taskService.getPagedTasks(pageable, null, null, null);

        verify(taskRepository).findAll(pageable);
    }

    @Test
    void testFindByDueDate() {
        when(taskRepository.findByDueDate("2025-01-01"))
                .thenReturn(List.of(new Task()));

        List<Task> result = taskService.findByDueDate("2025-01-01");

        assertEquals(1, result.size());
        verify(taskRepository).findByDueDate("2025-01-01");
    }

    @Test
    void testFindAllWithPageable() {
        Pageable pageable = PageRequest.of(0, 5);
        when(taskRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<Task> result = taskService.findAll(pageable);

        assertNotNull(result);
        verify(taskRepository).findAll(pageable);
    }
}

