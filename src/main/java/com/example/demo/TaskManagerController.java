package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TaskManagerController {

    private final TaskManagerService taskManagerService;

    public TaskManagerController(TaskManagerService taskManagerService) {
        this.taskManagerService = taskManagerService;
    }

    @GetMapping
    public Page<TaskManager> getAllTask(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return taskManagerService.getAllTask(page, size);
    }

    @GetMapping("/{id}")
    public TaskManager getTaskById(@PathVariable("id") Long id) {
        return taskManagerService.getTaskById(id);
    }

    @PostMapping
    public ResponseEntity<TaskManager> addNewTask(@Valid @RequestBody TaskManager taskManager) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskManagerService.addNewTask(taskManager));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable("id") Long id) {
        taskManagerService.deleteTask(id);
    }

    @PutMapping("/{id}")
    public TaskManager updateTask(@PathVariable("id") Long id, @RequestBody TaskManager updatedTask) {
        return taskManagerService.updateTask(id, updatedTask);
    }

    @PatchMapping("/{id}")
    public TaskManager partialUpdateTask(@PathVariable("id") Long id, @RequestBody TaskManager partialUpdatedTask) {
        return taskManagerService.partialUpdateTask(id, partialUpdatedTask);
    }

    @Autowired
    private DueTaskScheduler dueTaskScheduler;

    @GetMapping("/test/trigger-due-check")
    public ResponseEntity<String> triggerDueCheck() {
        dueTaskScheduler.checkDueTasks();
        return ResponseEntity.ok("Scheduler triggered");
    }
}
