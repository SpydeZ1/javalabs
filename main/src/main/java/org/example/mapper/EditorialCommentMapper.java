package org.example.mapper;

import org.example.dto.EditorialCommentRequest;
import org.example.dto.EditorialCommentResponse;
import org.example.dto.EditorialCommentUpdateRequest;
import org.example.model.EditorialComment;

public final class EditorialCommentMapper {

    private EditorialCommentMapper() {
    }

    public static EditorialComment toEntity(EditorialCommentRequest request) {
        EditorialComment comment = new EditorialComment();
        comment.setContent(request.getContent());
        return comment;
    }

    public static void apply(EditorialCommentUpdateRequest request, EditorialComment comment) {
        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }
    }

    public static EditorialCommentResponse toResponse(EditorialComment comment) {
        return new EditorialCommentResponse(
                comment.getId(),
                comment.getTranslation().getId(),
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
