package org.example.service;

import org.example.exception.BusinessException;
import org.example.model.ProjectStatus;
import org.example.model.SourceSegment;
import org.example.model.TranslationProject;
import org.example.repository.SourceSegmentRepository;
import org.example.repository.TranslationProjectRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TranslationProjectService {

    private final TranslationProjectRepository projectRepository;
    private final SourceSegmentRepository segmentRepository;
    private final SourceSegmentService segmentService;
    private final UserRepository userRepository;

    public TranslationProjectService(TranslationProjectRepository projectRepository,
                                     SourceSegmentRepository segmentRepository,
                                     SourceSegmentService segmentService,
                                     UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.segmentRepository = segmentRepository;
        this.segmentService = segmentService;
        this.userRepository = userRepository;
    }

    public List<TranslationProject> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<TranslationProject> getProjectById(Long id) {
        return Optional.ofNullable(projectRepository.findById(id));
    }

    public TranslationProject createProject(TranslationProject project) {
        requireOwner(project.getOwnerId());
        project.setId(null);
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.DRAFT);
        }
        return projectRepository.save(project);
    }

    public TranslationProject updateProject(Long id, TranslationProject details) {
        TranslationProject project = projectRepository.findById(id);
        if (project == null) {
            throw BusinessException.notFound("Project", id);
        }
        if (details.getName() != null) {
            if (details.getName().isBlank()) throw BusinessException.badRequest("name must not be blank");
            project.setName(details.getName());
        }
        if (details.getTargetLanguage() != null) {
            if (details.getTargetLanguage().isBlank()) throw BusinessException.badRequest("targetLanguage must not be blank");
            project.setTargetLanguage(details.getTargetLanguage());
        }
        if (details.getOwnerId() != null) {
            requireOwner(details.getOwnerId());
            project.setOwnerId(details.getOwnerId());
        }
        if (details.getStatus() != null) {
            project.setStatus(details.getStatus());
        }
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /** Удаляет проект вместе с сегментами, переводами и комментариями. */
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw BusinessException.notFound("Project", id);
        }
        for (SourceSegment segment : segmentRepository.findByProjectId(id)) {
            segmentService.deleteSegment(segment.getId());
        }
        projectRepository.deleteById(id);
    }

    public boolean projectExists(Long id) {
        return projectRepository.existsById(id);
    }

    private void requireOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw BusinessException.badRequest("Owner not found: " + ownerId);
        }
    }
}
