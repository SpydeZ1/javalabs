package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.TranslationProjectRequest;
import org.example.dto.TranslationProjectResponse;
import org.example.dto.TranslationProjectUpdateRequest;
import org.example.mapper.TranslationProjectMapper;
import org.example.model.TranslationProject;
import org.example.service.TranslationProjectService;
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
@RequestMapping("/api/translation-projects")
public class TranslationProjectController {

    private final TranslationProjectService projectService;

    public TranslationProjectController(TranslationProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<TranslationProjectResponse> getAllProjects() {
        return projectService.getAllProjects().stream()
                .map(TranslationProjectMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TranslationProjectResponse getProjectById(@PathVariable Long id) {
        return TranslationProjectMapper.toResponse(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<TranslationProjectResponse> createProject(
            @Valid @RequestBody TranslationProjectRequest request) {
        TranslationProject created = projectService.createProject(
                TranslationProjectMapper.toEntity(request), request.getOwnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TranslationProjectMapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public TranslationProjectResponse updateProject(
            @PathVariable Long id,
            @Valid @RequestBody TranslationProjectUpdateRequest request) {
        return TranslationProjectMapper.toResponse(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public TranslationProjectResponse startProject(@PathVariable Long id) {
        return TranslationProjectMapper.toResponse(projectService.startProject(id));
    }

    @PostMapping("/{id}/complete")
    public TranslationProjectResponse completeProject(@PathVariable Long id) {
        return TranslationProjectMapper.toResponse(projectService.completeProject(id));
    }
}
