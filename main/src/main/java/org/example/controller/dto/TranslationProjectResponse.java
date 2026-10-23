package org.example.controller.dto;

import org.example.model.ProjectStatus;

import java.time.LocalDateTime;

public record TranslationProjectResponse(Long id, String name, String targetLanguage, Long ownerId,
                                         ProjectStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
