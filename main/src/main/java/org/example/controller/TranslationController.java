package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.RequestChangesRequest;
import org.example.dto.RequestChangesResponse;
import org.example.dto.TranslationRequest;
import org.example.dto.TranslationResponse;
import org.example.dto.TranslationUpdateRequest;
import org.example.mapper.EditorialCommentMapper;
import org.example.mapper.TranslationMapper;
import org.example.model.EditorialComment;
import org.example.model.Translation;
import org.example.service.TranslationService;
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
@RequestMapping("/api/translations")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping
    public List<TranslationResponse> getAllTranslations() {
        return translationService.getAllTranslations().stream()
                .map(TranslationMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TranslationResponse getTranslationById(@PathVariable Long id) {
        return TranslationMapper.toResponse(translationService.getTranslationById(id));
    }

    @GetMapping("/segment/{segmentId}")
    public List<TranslationResponse> getTranslationsBySegmentId(@PathVariable Long segmentId) {
        return translationService.getTranslationsBySegmentId(segmentId).stream()
                .map(TranslationMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<TranslationResponse> createTranslation(@Valid @RequestBody TranslationRequest request) {
        Translation created = translationService.createTranslation(
                TranslationMapper.toEntity(request), request.getSegmentId(), request.getAuthorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TranslationMapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public TranslationResponse updateTranslation(
            @PathVariable Long id,
            @Valid @RequestBody TranslationUpdateRequest request) {
        return TranslationMapper.toResponse(translationService.updateTranslation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable Long id) {
        translationService.deleteTranslation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    public TranslationResponse submit(@PathVariable Long id) {
        return TranslationMapper.toResponse(translationService.submit(id));
    }

    @PostMapping("/{id}/approve")
    public TranslationResponse approve(@PathVariable Long id) {
        return TranslationMapper.toResponse(translationService.approve(id));
    }

    @PostMapping("/{id}/request-changes")
    public RequestChangesResponse requestChanges(
            @PathVariable Long id,
            @Valid @RequestBody RequestChangesRequest request) {
        EditorialComment comment = translationService.requestChanges(id, request.getContent());
        return new RequestChangesResponse(
                TranslationMapper.toResponse(comment.getTranslation()),
                EditorialCommentMapper.toResponse(comment)
        );
    }
}
