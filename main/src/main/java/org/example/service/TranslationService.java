package org.example.service;

import org.example.exception.BadRequestException;
import org.example.exception.ConflictException;
import org.example.dto.TranslationUpdateRequest;
import org.example.exception.NotFoundException;
import org.example.mapper.TranslationMapper;
import org.example.model.CommentStatus;
import org.example.model.EditorialComment;
import org.example.model.ProjectStatus;
import org.example.model.SourceSegment;
import org.example.model.Translation;
import org.example.model.TranslationStatus;
import org.example.model.User;
import org.example.repository.EditorialCommentRepository;
import org.example.repository.TranslationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final EditorialCommentRepository commentRepository;
    private final SourceSegmentService segmentService;
    private final UserService userService;

    public TranslationService(TranslationRepository translationRepository,
                              EditorialCommentRepository commentRepository,
                              SourceSegmentService segmentService,
                              UserService userService) {
        this.translationRepository = translationRepository;
        this.commentRepository = commentRepository;
        this.segmentService = segmentService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Translation> getAllTranslations() {
        return translationRepository.findAllWithRelations();
    }

    @Transactional(readOnly = true)
    public List<Translation> getTranslationsBySegmentId(Long segmentId) {
        segmentService.getSegmentById(segmentId);
        return translationRepository.findBySegmentId(segmentId);
    }

    @Transactional(readOnly = true)
    public Translation getTranslationById(Long id) {
        return translationRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new NotFoundException("Перевод не найден: " + id));
    }

    public Translation createTranslation(Translation translation, Long segmentId, Long authorId) {
        SourceSegment segment = segmentService.getSegmentById(segmentId);
        requireProjectInProgress(segment);
        if (translationRepository.existsBySegmentIdAndStatus(segmentId, TranslationStatus.APPROVED)) {
            throw new ConflictException("У сегмента уже есть утверждённый перевод");
        }
        User author = userService.getUserById(authorId);
        translation.setSegment(segment);
        translation.setAuthor(author);
        translation.setStatus(TranslationStatus.DRAFT);
        return translationRepository.save(translation);
    }

    public Translation updateTranslation(Long id, TranslationUpdateRequest request) {
        Translation translation = getTranslationById(id);
        requireEditableText(translation);
        if (request.getText() != null && request.getText().isBlank()) {
            throw new BadRequestException("Текст перевода не может быть пустым");
        }
        TranslationMapper.apply(request, translation);
        return translationRepository.saveAndFlush(translation);
    }

    public void deleteTranslation(Long id) {
        Translation translation = getTranslationById(id);
        if (translation.getStatus() == TranslationStatus.APPROVED) {
            throw new ConflictException("Утверждённый перевод нельзя удалить");
        }
        if (translation.getSegment().getProject().getStatus() == ProjectStatus.COMPLETED) {
            throw new ConflictException("В завершённом проекте переводы менять нельзя");
        }
        translationRepository.delete(translation);
    }

    public Translation submit(Long id) {
        Translation translation = getTranslationById(id);
        requireProjectInProgress(translation.getSegment());
        if (translation.getStatus() != TranslationStatus.DRAFT
                && translation.getStatus() != TranslationStatus.REJECTED) {
            throw new ConflictException("На проверку можно отправить только черновик или отклонённый перевод");
        }
        if (translation.getText() == null || translation.getText().isBlank()) {
            throw new ConflictException("Нельзя отправить пустой перевод");
        }
        if (commentRepository.existsByTranslationAndStatus(translation, CommentStatus.OPEN)) {
            throw new ConflictException("Сначала нужно закрыть все открытые замечания");
        }
        translation.setStatus(TranslationStatus.SUBMITTED);
        return translationRepository.save(translation);
    }

    public Translation approve(Long id) {
        Translation translation = translationRepository.findByIdWithComments(id)
                .orElseThrow(() -> new NotFoundException("Перевод не найден: " + id));
        requireProjectInProgress(translation.getSegment());
        if (translation.getStatus() != TranslationStatus.SUBMITTED) {
            throw new ConflictException("Утвердить можно только перевод на проверке");
        }
        if (translationRepository.existsBySegmentIdAndStatus(translation.getSegment().getId(), TranslationStatus.APPROVED)) {
            throw new ConflictException("У сегмента уже есть утверждённый перевод");
        }
        boolean hasOpenComments = translation.getComments().stream()
                .anyMatch(comment -> comment.getStatus() == CommentStatus.OPEN);
        if (hasOpenComments) {
            throw new ConflictException("Нельзя утвердить перевод, пока есть открытые замечания");
        }
        translation.setStatus(TranslationStatus.APPROVED);
        return translationRepository.save(translation);
    }

    public EditorialComment requestChanges(Long id, String content) {
        Translation translation = getTranslationById(id);
        requireProjectInProgress(translation.getSegment());
        if (translation.getStatus() != TranslationStatus.SUBMITTED) {
            throw new ConflictException("Замечания можно оставить только к переводу на проверке");
        }
        EditorialComment comment = new EditorialComment();
        comment.setTranslation(translation);
        comment.setContent(content);
        comment.setStatus(CommentStatus.OPEN);
        translation.setStatus(TranslationStatus.REJECTED);
        translationRepository.save(translation);
        return commentRepository.save(comment);
    }

    private void requireProjectInProgress(SourceSegment segment) {
        if (segment.getProject().getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new ConflictException("Переводы допустимы только для проекта в работе");
        }
    }

    private void requireEditableText(Translation translation) {
        requireProjectInProgress(translation.getSegment());
        if (translation.getStatus() != TranslationStatus.DRAFT
                && translation.getStatus() != TranslationStatus.REJECTED) {
            throw new ConflictException("Текст можно править только у черновика или отклонённого перевода");
        }
    }
}
