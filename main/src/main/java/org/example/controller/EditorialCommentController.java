package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.EditorialCommentRequest;
import org.example.controller.dto.EditorialCommentResponse;
import org.example.service.EditorialCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/editorial-comments")
public class EditorialCommentController {

    private final EditorialCommentService service;

    public EditorialCommentController(EditorialCommentService service) {
        this.service = service;
    }

    @GetMapping
    public List<EditorialCommentResponse> getAll() {
        return service.getAllComments().stream().map(Mappers::toCommentResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EditorialCommentResponse> getById(@PathVariable Long id) {
        return service.getCommentById(id)
                .map(Mappers::toCommentResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/translation/{translationId}")
    public List<EditorialCommentResponse> getByTranslationId(@PathVariable Long translationId) {
        return service.getCommentsByTranslationId(translationId).stream().map(Mappers::toCommentResponse).toList();
    }

    @PostMapping
    public ResponseEntity<EditorialCommentResponse> create(@Valid @RequestBody EditorialCommentRequest body) {
        var created = service.createComment(Mappers.toComment(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(Mappers.toCommentResponse(created));
    }

    /** Частичное обновление: поля, не переданные в теле, остаются без изменений. */
    @PatchMapping("/{id}")
    public EditorialCommentResponse update(@PathVariable Long id, @RequestBody EditorialCommentRequest body) {
        return Mappers.toCommentResponse(service.updateComment(id, Mappers.toComment(body)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
