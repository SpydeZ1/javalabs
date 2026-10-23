package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.TranslationRequest;
import org.example.controller.dto.TranslationResponse;
import org.example.service.TranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    private final TranslationService service;

    public TranslationController(TranslationService service) {
        this.service = service;
    }

    @GetMapping
    public List<TranslationResponse> getAll() {
        return service.getAllTranslations().stream().map(Mappers::toTranslationResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TranslationResponse> getById(@PathVariable Long id) {
        return service.getTranslationById(id)
                .map(Mappers::toTranslationResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/segment/{segmentId}")
    public List<TranslationResponse> getBySegmentId(@PathVariable Long segmentId) {
        return service.getTranslationsBySegmentId(segmentId).stream().map(Mappers::toTranslationResponse).toList();
    }

    @PostMapping
    public ResponseEntity<TranslationResponse> create(@Valid @RequestBody TranslationRequest body) {
        var created = service.createTranslation(Mappers.toTranslation(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(Mappers.toTranslationResponse(created));
    }

    /** Частичное обновление: поля, не переданные в теле, остаются без изменений. */
    @PatchMapping("/{id}")
    public TranslationResponse update(@PathVariable Long id, @RequestBody TranslationRequest body) {
        return Mappers.toTranslationResponse(service.updateTranslation(id, Mappers.toTranslation(body)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTranslation(id);
        return ResponseEntity.noContent().build();
    }
}
