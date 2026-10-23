package org.example.service;

import org.example.exception.BusinessException;
import org.example.model.CommentStatus;
import org.example.model.EditorialComment;
import org.example.repository.EditorialCommentRepository;
import org.example.repository.TranslationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EditorialCommentService {

    private final EditorialCommentRepository commentRepository;
    private final TranslationRepository translationRepository;

    public EditorialCommentService(EditorialCommentRepository commentRepository,
                                   TranslationRepository translationRepository) {
        this.commentRepository = commentRepository;
        this.translationRepository = translationRepository;
    }

    public List<EditorialComment> getAllComments() {
        return commentRepository.findAll();
    }

    public List<EditorialComment> getCommentsByTranslationId(Long translationId) {
        return commentRepository.findByTranslationId(translationId);
    }

    public Optional<EditorialComment> getCommentById(Long id) {
        return Optional.ofNullable(commentRepository.findById(id));
    }

    public EditorialComment createComment(EditorialComment comment) {
        requireTranslation(comment.getTranslationId());
        comment.setId(null);
        if (comment.getStatus() == null) {
            comment.setStatus(CommentStatus.OPEN);
        }
        return commentRepository.save(comment);
    }

    public EditorialComment updateComment(Long id, EditorialComment details) {
        EditorialComment comment = commentRepository.findById(id);
        if (comment == null) {
            throw BusinessException.notFound("Comment", id);
        }
        if (details.getTranslationId() != null) {
            requireTranslation(details.getTranslationId());
            comment.setTranslationId(details.getTranslationId());
        }
        if (details.getCommentText() != null) {
            if (details.getCommentText().isBlank()) throw BusinessException.badRequest("commentText must not be blank");
            comment.setCommentText(details.getCommentText());
        }
        if (details.getStatus() != null) {
            comment.setStatus(details.getStatus());
        }
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw BusinessException.notFound("Comment", id);
        }
        commentRepository.deleteById(id);
    }

    public boolean commentExists(Long id) {
        return commentRepository.existsById(id);
    }

    private void requireTranslation(Long translationId) {
        if (!translationRepository.existsById(translationId)) {
            throw BusinessException.badRequest("Translation not found: " + translationId);
        }
    }
}
