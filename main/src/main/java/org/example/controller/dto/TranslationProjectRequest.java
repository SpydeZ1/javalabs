package org.example.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.model.ProjectStatus;

public record TranslationProjectRequest(
        @NotBlank String name,
        @NotBlank String targetLanguage,
        @NotNull Long ownerId,
        ProjectStatus status) {
}
