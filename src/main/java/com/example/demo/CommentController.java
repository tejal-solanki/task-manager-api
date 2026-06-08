// src/main/java/com/example/demo/CommentController.java
package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/task/{taskId}/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final TaskManagerRepository taskManagerRepository;

    public CommentController(CommentRepository commentRepository,
                             TaskManagerRepository taskManagerRepository) {
        this.commentRepository = commentRepository;
        this.taskManagerRepository = taskManagerRepository;
    }

    @GetMapping
    public List<Comment> getComments(@PathVariable Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Long taskId,
            @RequestBody CommentRequest request) {

        taskManagerRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task " + taskId + " not found"));

        String author = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Comment comment = new Comment(taskId, request.content(), author);
        return ResponseEntity.ok(commentRepository.save(comment));
    }
}