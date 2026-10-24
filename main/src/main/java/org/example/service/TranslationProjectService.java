package org.example.service;

import org.example.dto.TranslationProjectUpdateRequest;
import org.example.exception.BadRequestException;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.mapper.TranslationProjectMapper;
import org.example.model.ProjectStatus;
import org.example.model.TranslationProject;
import org.example.model.User;
import org.example.repository.SourceSegmentRepository;
import org.example.repository.TranslationProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TranslationProjectService {

    private final TranslationProjectRepository projectRepository;
    private final SourceSegmentRepository segmentRepository;
    private final UserService userService;

    public TranslationProjectService(TranslationProjectRepository projectRepository,
                                     SourceSegmentRepository segmentRepository,
                                     UserService userService) {
        this.projectRepository = projectRepository;
        this.segmentRepository = segmentRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<TranslationProject> getAllProjects() {
        return projectRepository.findAllWithOwner();
    }

    @Transactional(readOnly = true)
    public TranslationProject getProjectById(Long id) {
        return projectRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new NotFoundException("Проект не найден: " + id));
    }

    public TranslationProject createProject(TranslationProject project, Long ownerId) {
        User owner = userService.getUserById(ownerId);
        project.setOwner(owner);
        project.setStatus(ProjectStatus.DRAFT);
        return projectRepository.save(project);
    }

    public TranslationProject updateProject(Long id, TranslationProjectUpdateRequest request) {
        TranslationProject project = getProjectById(id);
        assertEditable(project);
        if (request.getName() != null && request.getName().isBlank()) {
            throw new BadRequestException("Название проекта не может быть пустым");
        }
        if (request.getTargetLanguage() != null && request.getTargetLanguage().isBlank()) {
            throw new BadRequestException("Целевой язык не может быть пустым");
        }
        TranslationProjectMapper.apply(request, project);
        if (request.getOwnerId() != null) {
            project.setOwner(userService.getUserById(request.getOwnerId()));
        }
        return projectRepository.saveAndFlush(project);
    }

    public void deleteProject(Long id) {
        TranslationProject project = getProjectById(id);
        projectRepository.delete(project);
    }

    public TranslationProject startProject(Long id) {
        TranslationProject project = getProjectById(id);
        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new ConflictException("Запустить можно только проект в статусе DRAFT");
        }
        if (segmentRepository.countByProject(project) == 0) {
            throw new ConflictException("Нельзя запустить проект без исходных сегментов");
        }
        project.setStatus(ProjectStatus.IN_PROGRESS);
        return projectRepository.save(project);
    }

    public TranslationProject completeProject(Long id) {
        TranslationProject project = getProjectById(id);
        if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new ConflictException("Завершить можно только проект в статусе IN_PROGRESS");
        }
        if (segmentRepository.countByProject(project) == 0) {
            throw new ConflictException("Нельзя завершить проект без сегментов");
        }
        if (segmentRepository.countWithoutApprovedTranslation(id) > 0) {
            throw new ConflictException("Завершение возможно, только если у каждого сегмента есть утверждённый перевод");
        }
        project.setStatus(ProjectStatus.COMPLETED);
        return projectRepository.save(project);
    }

    public void assertEditable(TranslationProject project) {
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new ConflictException("Завершённый проект нельзя изменять");
        }
    }
}
