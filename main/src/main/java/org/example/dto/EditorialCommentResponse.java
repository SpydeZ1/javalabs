package org.example.dto;

import org.example.model.CommentStatus;

import java.time.LocalDateTime;

public class EditorialCommentResponse {
    private Long id;
    private Long translationId;
    private String content;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EditorialCommentResponse() {
    }

    public EditorialCommentResponse(Long id, Long translationId, String content, CommentStatus status,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.translationId = translationId;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTranslationId() {
        return translationId;
    }

    public String getContent() {
        return content;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
