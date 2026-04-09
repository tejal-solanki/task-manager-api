package com.example.demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskManagerService {

    private final TaskManagerRepository taskManagerRepository;

    public TaskManagerService(TaskManagerRepository taskManagerRepository) {
        this.taskManagerRepository = taskManagerRepository;
    }

    public Page<TaskManager> getAllTask(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskManagerRepository.findAll(pageable);
    }

    public TaskManager addNewTask(TaskManager taskManager) {
        return taskManagerRepository.save(taskManager);
    }

    public TaskManager getTaskById(Long id) {
        return taskManagerRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
    }

    public void deleteTask(Long id) {
        taskManagerRepository.deleteById(id);
    }

    public TaskManager updateTask(Long id, TaskManager updatedTask) {
        TaskManager task = taskManagerRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " is not found"));

        task.setTitle(updatedTask.getTitle());
        task.setStatus(updatedTask.getStatus());
        task.setCreatedAt(updatedTask.getCreatedAt());
        task.setDescription(updatedTask.getDescription());

        return taskManagerRepository.save(task);
    }

    public TaskManager partialUpdateTask(Long id, TaskManager updatedTask) {
        TaskManager task = taskManagerRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " is not found"));

        if (updatedTask.getTitle() != null)
            task.setTitle(updatedTask.getTitle());
        if (updatedTask.getStatus() != null)
            task.setStatus(updatedTask.getStatus());
        if (updatedTask.getCreatedAt() != null)
            task.setCreatedAt(updatedTask.getCreatedAt());
        if (updatedTask.getDescription() != null)
            task.setDescription(updatedTask.getDescription());

        return taskManagerRepository.save(task);
    }
}
