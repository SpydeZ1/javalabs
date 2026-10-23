package org.example.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SourceSegmentRequest(
        @NotNull Long projectId,
        @NotBlank String sourceText,
        @NotNull @Min(1) Integer order) {
}
