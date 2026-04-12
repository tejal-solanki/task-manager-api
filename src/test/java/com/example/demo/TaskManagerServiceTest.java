package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TaskManagerServiceTest {

    @MockitoBean
    private TaskManagerRepository taskManagerRepository;

    @MockitoBean
    private TaskMessagePublisher taskMessagePublisher;

    @Autowired
    private TaskManagerService taskManagerService;

    @Test
    void addNewTask_shouldSaveAndReturnTask() {
        TaskManager input = new TaskManager("Test task", "Desc", TaskStatus.TODO, LocalDate.now());
        when(taskManagerRepository.save(input)).thenReturn(input);

        TaskManager result = taskManagerService.addNewTask(input);

        assertEquals("Test task", result.getTitle());
        verify(taskManagerRepository, times(1)).save(input);
        verify(taskMessagePublisher, times(1)).publishTaskCreated(anyString());
    }

    @Test
    void getTaskById_shouldThrowWhenNotFound() {
        when(taskManagerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            taskManagerService.getTaskById(99L);
        });
    }
}