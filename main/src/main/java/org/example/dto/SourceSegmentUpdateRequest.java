package org.example.dto;

import jakarta.validation.constraints.Min;

public class SourceSegmentUpdateRequest {
    private String content;

    @Min(1)
    private Integer sequenceNumber;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
