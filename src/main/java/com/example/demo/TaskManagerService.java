package com.example.demo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskManagerService {

    private final TaskManagerRepository taskManagerRepository;
    private final TaskMessagePublisher taskMessagePublisher;
    private final PriorityPredictionService priorityPredictionService;

    public TaskManagerService(TaskManagerRepository taskManagerRepository,
            TaskMessagePublisher taskMessagePublisher,
            PriorityPredictionService priorityPredictionService) {
        this.taskManagerRepository = taskManagerRepository;
        this.taskMessagePublisher = taskMessagePublisher;
        this.priorityPredictionService = priorityPredictionService;
    }

    public Page<TaskManager> getAllTask(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskManagerRepository.findAll(pageable);
    }

    public TaskManager addNewTask(TaskManager taskManager, String username) {
        String predicted = priorityPredictionService.predictPriority(
                taskManager.getTitle(),
                taskManager.getDescription() != null ? taskManager.getDescription() : "");
        taskManager.setPriority(predicted);
        taskManager.setCreatedBy(username);
        System.out.println("[AI] Predicted priority: " + predicted + " for task: " + taskManager.getTitle());

        TaskManager saved = taskManagerRepository.save(taskManager);

        taskMessagePublisher.publishTaskCreated("Task created: " + saved.getTitle());

        if ("HIGH".equals(predicted)) {
            taskMessagePublisher.publishUrgentTask(
                    "URGENT: High priority task created — " + saved.getTitle());
        }

        return saved;
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

    public void deleteTask(Long id, String username, String role) {
        TaskManager task = taskManagerRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(role);
        boolean isOwner = username.equals(task.getCreatedBy());

        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only delete your own tasks");
        }

        taskManagerRepository.deleteById(id);
    }
}