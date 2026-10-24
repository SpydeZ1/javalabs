package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.EditorialCommentRequest;
import org.example.dto.EditorialCommentResponse;
import org.example.dto.EditorialCommentUpdateRequest;
import org.example.mapper.EditorialCommentMapper;
import org.example.model.EditorialComment;
import org.example.service.EditorialCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/editorial-comments")
public class EditorialCommentController {

    private final EditorialCommentService commentService;

    public EditorialCommentController(EditorialCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<EditorialCommentResponse> getAllComments() {
        return commentService.getAllComments().stream()
                .map(EditorialCommentMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EditorialCommentResponse getCommentById(@PathVariable Long id) {
        return EditorialCommentMapper.toResponse(commentService.getCommentById(id));
    }

    @GetMapping("/translation/{translationId}")
    public List<EditorialCommentResponse> getCommentsByTranslationId(@PathVariable Long translationId) {
        return commentService.getCommentsByTranslationId(translationId).stream()
                .map(EditorialCommentMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<EditorialCommentResponse> createComment(
            @Valid @RequestBody EditorialCommentRequest request) {
        EditorialComment created = commentService.createComment(
                EditorialCommentMapper.toEntity(request), request.getTranslationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EditorialCommentMapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public EditorialCommentResponse updateComment(
            @PathVariable Long id,
            @Valid @RequestBody EditorialCommentUpdateRequest request) {
        return EditorialCommentMapper.toResponse(commentService.updateComment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resolve")
    public EditorialCommentResponse resolve(@PathVariable Long id) {
        return EditorialCommentMapper.toResponse(commentService.resolve(id));
    }
}
