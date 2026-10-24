package org.example.dto;

import java.time.LocalDateTime;

public class SourceSegmentResponse {
    private Long id;
    private Long projectId;
    private String content;
    private Integer sequenceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SourceSegmentResponse() {
    }

    public SourceSegmentResponse(Long id, Long projectId, String content, Integer sequenceNumber,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.content = content;
        this.sequenceNumber = sequenceNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getContent() {
        return content;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
