package org.example.dto;

import org.example.model.TranslationStatus;

import java.time.LocalDateTime;

public class TranslationResponse {
    private Long id;
    private Long segmentId;
    private Long authorId;
    private String text;
    private TranslationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TranslationResponse() {
    }

    public TranslationResponse(Long id, Long segmentId, Long authorId, String text,
                               TranslationStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.segmentId = segmentId;
        this.authorId = authorId;
        this.text = text;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSegmentId() {
        return segmentId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public TranslationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
