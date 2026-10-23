package org.example.controller;

import org.example.model.EditorialComment;
import org.example.service.EditorialCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/editorial-comments")
public class EditorialCommentController {

    @Autowired
    private EditorialCommentService commentService;

    @GetMapping
    public List<EditorialComment> getAllComments() {
        return commentService.getAllComments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EditorialComment> getCommentById(@PathVariable Long id) {
        Optional<EditorialComment> comment = commentService.getCommentById(id);
        return comment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/translation/{translationId}")
    public List<EditorialComment> getCommentsByTranslationId(@PathVariable Long translationId) {
        return commentService.getCommentsByTranslationId(translationId);
    }

    @PostMapping
    public ResponseEntity<EditorialComment> createComment(@RequestBody EditorialComment comment) {
        EditorialComment createdComment = commentService.createComment(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EditorialComment> updateComment(@PathVariable Long id, @RequestBody EditorialComment commentDetails) {
        EditorialComment updatedComment = commentService.updateComment(id, commentDetails);
        if (updatedComment != null) {
            return ResponseEntity.ok(updatedComment);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}