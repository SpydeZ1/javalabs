package org.example.mapper;

import org.example.dto.TranslationRequest;
import org.example.dto.TranslationResponse;
import org.example.dto.TranslationUpdateRequest;
import org.example.model.Translation;

public final class TranslationMapper {

    private TranslationMapper() {
    }

    public static Translation toEntity(TranslationRequest request) {
        Translation translation = new Translation();
        translation.setText(request.getText());
        return translation;
    }

    public static void apply(TranslationUpdateRequest request, Translation translation) {
        if (request.getText() != null) {
            translation.setText(request.getText());
        }
    }

    public static TranslationResponse toResponse(Translation translation) {
        return new TranslationResponse(
                translation.getId(),
                translation.getSegment().getId(),
                translation.getAuthor().getId(),
                translation.getText(),
                translation.getStatus(),
                translation.getCreatedAt(),
                translation.getUpdatedAt()
        );
    }
}
