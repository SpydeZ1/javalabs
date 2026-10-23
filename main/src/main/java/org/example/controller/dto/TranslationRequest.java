package org.example.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.model.TranslationStatus;

public record TranslationRequest(
        @NotNull Long segmentId,
        @NotNull Long authorId,
        @NotBlank String text,
        TranslationStatus status) {
}
