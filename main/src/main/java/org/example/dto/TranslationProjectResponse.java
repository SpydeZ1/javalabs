package org.example.dto;

import org.example.model.ProjectStatus;

import java.time.LocalDateTime;

public class TranslationProjectResponse {
    private Long id;
    private String name;
    private String targetLanguage;
    private Long ownerId;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TranslationProjectResponse() {
    }

    public TranslationProjectResponse(Long id, String name, String targetLanguage, Long ownerId,
                                      ProjectStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.targetLanguage = targetLanguage;
        this.ownerId = ownerId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
