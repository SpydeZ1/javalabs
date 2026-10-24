package org.example.mapper;

import org.example.dto.TranslationProjectRequest;
import org.example.dto.TranslationProjectResponse;
import org.example.dto.TranslationProjectUpdateRequest;
import org.example.model.TranslationProject;

public final class TranslationProjectMapper {

    private TranslationProjectMapper() {
    }

    public static TranslationProject toEntity(TranslationProjectRequest request) {
        TranslationProject project = new TranslationProject();
        project.setName(request.getName());
        project.setTargetLanguage(request.getTargetLanguage());
        return project;
    }

    public static void apply(TranslationProjectUpdateRequest request, TranslationProject project) {
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getTargetLanguage() != null) {
            project.setTargetLanguage(request.getTargetLanguage());
        }
    }

    public static TranslationProjectResponse toResponse(TranslationProject project) {
        return new TranslationProjectResponse(
                project.getId(),
                project.getName(),
                project.getTargetLanguage(),
                project.getOwner().getId(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
