package org.example.service;

import org.example.exception.BadRequestException;
import org.example.exception.ConflictException;
import org.example.dto.EditorialCommentUpdateRequest;
import org.example.exception.NotFoundException;
import org.example.mapper.EditorialCommentMapper;
import org.example.model.CommentStatus;
import org.example.model.EditorialComment;
import org.example.model.ProjectStatus;
import org.example.model.Translation;
import org.example.model.TranslationStatus;
import org.example.repository.EditorialCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EditorialCommentService {

    private final EditorialCommentRepository commentRepository;
    private final TranslationService translationService;

    public EditorialCommentService(EditorialCommentRepository commentRepository,
                                   TranslationService translationService) {
        this.commentRepository = commentRepository;
        this.translationService = translationService;
    }

    @Transactional(readOnly = true)
    public List<EditorialComment> getAllComments() {
        return commentRepository.findAllWithTranslation();
    }

    @Transactional(readOnly = true)
    public List<EditorialComment> getCommentsByTranslationId(Long translationId) {
        translationService.getTranslationById(translationId);
        return commentRepository.findByTranslationId(translationId);
    }

    @Transactional(readOnly = true)
    public EditorialComment getCommentById(Long id) {
        return commentRepository.findByIdWithTranslation(id)
                .orElseThrow(() -> new NotFoundException("Замечание не найдено: " + id));
    }

    public EditorialComment createComment(EditorialComment comment, Long translationId) {
        Translation translation = translationService.getTranslationById(translationId);
        requireActiveReview(translation);
        if (translation.getStatus() == TranslationStatus.SUBMITTED) {
            // как и при request-changes: перевод возвращается автору на доработку
            translation.setStatus(TranslationStatus.REJECTED);
        } else if (translation.getStatus() != TranslationStatus.REJECTED) {
            throw new ConflictException("Замечание можно добавить только к переводу на проверке или возвращённому на доработку");
        }
        comment.setTranslation(translation);
        comment.setStatus(CommentStatus.OPEN);
        return commentRepository.save(comment);
    }

    public EditorialComment updateComment(Long id, EditorialCommentUpdateRequest request) {
        EditorialComment comment = getCommentById(id);
        if (comment.getStatus() != CommentStatus.OPEN) {
            throw new ConflictException("Редактировать можно только открытое замечание");
        }
        if (request.getContent() != null && request.getContent().isBlank()) {
            throw new BadRequestException("Текст замечания не может быть пустым");
        }
        requireActiveReview(comment.getTranslation());
        EditorialCommentMapper.apply(request, comment);
        return commentRepository.saveAndFlush(comment);
    }

    public void deleteComment(Long id) {
        EditorialComment comment = getCommentById(id);
        requireActiveReview(comment.getTranslation());
        commentRepository.delete(comment);
    }

    public EditorialComment resolve(Long id) {
        EditorialComment comment = getCommentById(id);
        requireActiveReview(comment.getTranslation());
        if (comment.getStatus() != CommentStatus.OPEN) {
            throw new ConflictException("Закрыть можно только открытое замечание");
        }
        comment.setStatus(CommentStatus.RESOLVED);
        return commentRepository.save(comment);
    }

    private void requireActiveReview(Translation translation) {
        if (translation.getSegment().getProject().getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new ConflictException("Замечания допустимы только для проекта в работе");
        }
        if (translation.getStatus() == TranslationStatus.APPROVED) {
            throw new ConflictException("Нельзя менять замечания к утверждённому переводу");
        }
    }
}
