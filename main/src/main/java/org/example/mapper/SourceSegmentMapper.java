package org.example.mapper;

import org.example.dto.SourceSegmentRequest;
import org.example.dto.SourceSegmentResponse;
import org.example.dto.SourceSegmentUpdateRequest;
import org.example.model.SourceSegment;

public final class SourceSegmentMapper {

    private SourceSegmentMapper() {
    }

    public static SourceSegment toEntity(SourceSegmentRequest request) {
        SourceSegment segment = new SourceSegment();
        segment.setContent(request.getContent());
        segment.setSequenceNumber(request.getSequenceNumber());
        return segment;
    }

    public static void apply(SourceSegmentUpdateRequest request, SourceSegment segment) {
        if (request.getContent() != null) {
            segment.setContent(request.getContent());
        }
        if (request.getSequenceNumber() != null) {
            segment.setSequenceNumber(request.getSequenceNumber());
        }
    }

    public static SourceSegmentResponse toResponse(SourceSegment segment) {
        return new SourceSegmentResponse(
                segment.getId(),
                segment.getProject().getId(),
                segment.getContent(),
                segment.getSequenceNumber(),
                segment.getCreatedAt(),
                segment.getUpdatedAt()
        );
    }
}
