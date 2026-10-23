package org.example.controller;

import org.example.controller.dto.*;
import org.example.model.*;

/**
 * Маппинг между HTTP-представлением (DTO) и доменными объектами.
 * Принадлежит слою контроллеров: сервисы и репозитории с DTO не работают.
 * id и временные поля из запроса не читаются - их задаёт сервер.
 */
final class Mappers {

    private Mappers() {
    }

    // --- User ---
    static User toUser(UserRequest r) {
        return new User(null, r.username(), r.email(), r.password(), r.role());
    }

    static UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(),
                u.getCreatedAt(), u.getUpdatedAt());
    }

    // --- TranslationProject ---
    static TranslationProject toProject(TranslationProjectRequest r) {
        return new TranslationProject(null, r.name(), r.targetLanguage(), r.ownerId(), r.status());
    }

    static TranslationProjectResponse toProjectResponse(TranslationProject p) {
        return new TranslationProjectResponse(p.getId(), p.getName(), p.getTargetLanguage(), p.getOwnerId(),
                p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }

    // --- SourceSegment ---
    static SourceSegment toSegment(SourceSegmentRequest r) {
        return new SourceSegment(null, r.projectId(), r.sourceText(), r.order());
    }

    static SourceSegmentResponse toSegmentResponse(SourceSegment s) {
        return new SourceSegmentResponse(s.getId(), s.getProjectId(), s.getSourceText(), s.getOrder(),
                s.getCreatedAt(), s.getUpdatedAt());
    }

    // --- Translation ---
    static Translation toTranslation(TranslationRequest r) {
        return new Translation(null, r.segmentId(), r.authorId(), r.text(), r.status());
    }

    static TranslationResponse toTranslationResponse(Translation t) {
        return new TranslationResponse(t.getId(), t.getSegmentId(), t.getAuthorId(), t.getText(),
                t.getStatus(), t.getCreatedAt(), t.getUpdatedAt());
    }

    // --- EditorialComment ---
    static EditorialComment toComment(EditorialCommentRequest r) {
        return new EditorialComment(null, r.translationId(), r.commentText(), r.status());
    }

    static EditorialCommentResponse toCommentResponse(EditorialComment c) {
        return new EditorialCommentResponse(c.getId(), c.getTranslationId(), c.getCommentText(),
                c.getStatus(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
