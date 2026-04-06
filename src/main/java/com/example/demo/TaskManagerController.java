package com.example.demo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<TaskManager> getAllTask() {
        return taskManagerService.getAllTask();
    }

    @GetMapping("/{id}")
    public TaskManager getTaskById( @PathVariable("id") Long id) {
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

    @PutMapping("/{id}/status")
    public TaskManager updateTaskStatus(@PathVariable("id") Long id, @RequestParam TaskStatus status) {
        return taskManagerService.setStatus(status, id);
    }
}
