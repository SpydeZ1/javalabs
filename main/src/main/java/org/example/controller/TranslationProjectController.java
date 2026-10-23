package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.TranslationProjectRequest;
import org.example.controller.dto.TranslationProjectResponse;
import org.example.service.TranslationProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/translation-projects")
public class TranslationProjectController {

    private final TranslationProjectService service;

    public TranslationProjectController(TranslationProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<TranslationProjectResponse> getAll() {
        return service.getAllProjects().stream().map(Mappers::toProjectResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TranslationProjectResponse> getById(@PathVariable Long id) {
        return service.getProjectById(id)
                .map(Mappers::toProjectResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TranslationProjectResponse> create(@Valid @RequestBody TranslationProjectRequest body) {
        var created = service.createProject(Mappers.toProject(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(Mappers.toProjectResponse(created));
    }

    /** Частичное обновление: поля, не переданные в теле, остаются без изменений. */
    @PatchMapping("/{id}")
    public TranslationProjectResponse update(@PathVariable Long id, @RequestBody TranslationProjectRequest body) {
        return Mappers.toProjectResponse(service.updateProject(id, Mappers.toProject(body)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
