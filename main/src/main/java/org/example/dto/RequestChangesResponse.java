package org.example.dto;

public class RequestChangesResponse {

    private TranslationResponse translation;
    private EditorialCommentResponse comment;

    public RequestChangesResponse(TranslationResponse translation, EditorialCommentResponse comment) {
        this.translation = translation;
        this.comment = comment;
    }

    public TranslationResponse getTranslation() {
        return translation;
    }

    public EditorialCommentResponse getComment() {
        return comment;
    }
}
