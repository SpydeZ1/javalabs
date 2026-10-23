package org.example.controller;

import org.example.model.Translation;
import org.example.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @GetMapping
    public List<Translation> getAllTranslations() {
        return translationService.getAllTranslations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Translation> getTranslationById(@PathVariable Long id) {
        Optional<Translation> translation = translationService.getTranslationById(id);
        return translation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/segment/{segmentId}")
    public List<Translation> getTranslationsBySegmentId(@PathVariable Long segmentId) {
        return translationService.getTranslationsBySegmentId(segmentId);
    }

    @PostMapping
    public ResponseEntity<Translation> createTranslation(@RequestBody Translation translation) {
        Translation createdTranslation = translationService.createTranslation(translation);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTranslation);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Translation> updateTranslation(@PathVariable Long id, @RequestBody Translation translationDetails) {
        Translation updatedTranslation = translationService.updateTranslation(id, translationDetails);
        if (updatedTranslation != null) {
            return ResponseEntity.ok(updatedTranslation);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable Long id) {
        translationService.deleteTranslation(id);
        return ResponseEntity.noContent().build();
    }
}