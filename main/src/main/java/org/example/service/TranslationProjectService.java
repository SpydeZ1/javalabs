package org.example.service;

import org.example.model.TranslationProject;
import org.example.repository.TranslationProjectRepository;
import org.example.repository.SourceSegmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TranslationProjectService {

    @Autowired
    private TranslationProjectRepository projectRepository;

    @Autowired
    private SourceSegmentRepository segmentRepository;

    public List<TranslationProject> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<TranslationProject> getProjectById(Long id) {
        return Optional.ofNullable(projectRepository.findById(id));
    }

    public TranslationProject createProject(TranslationProject project) {
        return projectRepository.save(project);
    }

    public TranslationProject updateProject(Long id, TranslationProject projectDetails) {
        TranslationProject project = projectRepository.findById(id);
        if (project != null) {
            project.setName(projectDetails.getName());
            project.setTargetLanguage(projectDetails.getTargetLanguage());
            project.setOwner(projectDetails.getOwner());
            project.setStatus(projectDetails.getStatus());
            return projectRepository.save(project);
        }
        return null;
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public boolean projectExists(Long id) {
        return projectRepository.existsById(id);
    }
}