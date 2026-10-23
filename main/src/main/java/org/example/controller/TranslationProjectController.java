package org.example.controller;

import org.example.model.TranslationProject;
import org.example.service.TranslationProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/translation-projects")
public class TranslationProjectController {

    @Autowired
    private TranslationProjectService projectService;

    @GetMapping
    public List<TranslationProject> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TranslationProject> getProjectById(@PathVariable Long id) {
        Optional<TranslationProject> project = projectService.getProjectById(id);
        return project.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TranslationProject> createProject(@RequestBody TranslationProject project) {
        TranslationProject createdProject = projectService.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TranslationProject> updateProject(@PathVariable Long id, @RequestBody TranslationProject projectDetails) {
        TranslationProject updatedProject = projectService.updateProject(id, projectDetails);
        if (updatedProject != null) {
            return ResponseEntity.ok(updatedProject);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}