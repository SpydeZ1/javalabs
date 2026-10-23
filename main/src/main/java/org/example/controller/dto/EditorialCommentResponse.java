package org.example.controller.dto;

import org.example.model.CommentStatus;

import java.time.LocalDateTime;

public record EditorialCommentResponse(Long id, Long translationId, String commentText,
                                       CommentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
