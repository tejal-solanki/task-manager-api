package com.example.demo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Service
public class TaskManagerService {

    private final TaskManagerRepository taskManagerRepository;

    public TaskManagerService(TaskManagerRepository taskManagerRepository) {
        this.taskManagerRepository = taskManagerRepository;
    }

    public List<TaskManager> getAllTask() {
        return taskManagerRepository.findAll();
    }

    public TaskManager addNewTask(TaskManager taskManager) {
        return taskManagerRepository.save(taskManager);
    }

    public TaskManager getTaskById(Long id) {
        return taskManagerRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
    }

    public TaskManager setStatus(TaskStatus status, Long id) {
        TaskManager task = taskManagerRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Status with id " + id + " not found"));
        ;
        if (status != null && !Objects.equals(task.getStatus(), status)) {
            task.setStatus(status);
        }
        return taskManagerRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskManagerRepository.deleteById(id);
    }

}
