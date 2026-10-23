package org.example.controller.dto;

import org.example.model.TranslationStatus;

import java.time.LocalDateTime;

public record TranslationResponse(Long id, Long segmentId, Long authorId, String text,
                                  TranslationStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
