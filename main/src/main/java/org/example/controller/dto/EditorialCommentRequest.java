package org.example.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.model.CommentStatus;

public record EditorialCommentRequest(
        @NotNull Long translationId,
        @NotBlank String commentText,
        CommentStatus status) {
}
