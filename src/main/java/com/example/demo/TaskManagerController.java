package com.example.demo;

import java.util.List;
import java.util.Map;

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
    private final PriorityPredictionService priorityPredictionService;
    private final TaskManagerRepository taskManagerRepository;

    public TaskManagerController(TaskManagerService taskManagerService,
            PriorityPredictionService priorityPredictionService, TaskManagerRepository taskManagerRepository) {
        this.taskManagerService = taskManagerService;
        this.priorityPredictionService = priorityPredictionService;
        this.taskManagerRepository = taskManagerRepository;
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @PutMapping("/admin/users/{username}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable String username,
            @RequestParam String role) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(Role.valueOf(role));
        appUserRepository.save(user);
        return ResponseEntity.ok("Role updated to " + role);
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
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskManagerService.addNewTask(taskManager, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        taskManagerService.deleteTask(id, username, role);
        return ResponseEntity.ok().build();
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

    @PostMapping("/suggest-description")
    public ResponseEntity<Map<String, String>> suggestDescription(
            @RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "");
        String suggestion = priorityPredictionService.suggestDescription(title);
        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }

    @PostMapping("/similar")
    public ResponseEntity<List<Map<String, Object>>> findSimilar(
            @RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "");
        double[] embedding = priorityPredictionService.generateEmbedding(title);

        if (embedding == null)
            return ResponseEntity.ok(List.of());

        String vectorStr = "[" + java.util.Arrays.stream(embedding)
                .mapToObj(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",")) + "]";

        List<TaskManager> similar = taskManagerRepository.findSimilarTasks(vectorStr, -1L);

        List<Map<String, Object>> result = similar.stream().map(t -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", t.getId());
            m.put("title", t.getTitle());
            m.put("status", t.getStatus());
            m.put("priority", t.getPriority());
            return m;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
