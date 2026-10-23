package org.example.controller.dto;

import java.time.LocalDateTime;

public record SourceSegmentResponse(Long id, Long projectId, String sourceText, Integer order,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
}
