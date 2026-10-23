package org.example.service;

import org.example.exception.BusinessException;
import org.example.model.Translation;
import org.example.repository.EditorialCommentRepository;
import org.example.repository.SourceSegmentRepository;
import org.example.repository.TranslationRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.model.TranslationStatus.DRAFT;

@Service
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final SourceSegmentRepository segmentRepository;
    private final UserRepository userRepository;
    private final EditorialCommentRepository commentRepository;

    public TranslationService(TranslationRepository translationRepository,
                              SourceSegmentRepository segmentRepository,
                              UserRepository userRepository,
                              EditorialCommentRepository commentRepository) {
        this.translationRepository = translationRepository;
        this.segmentRepository = segmentRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public List<Translation> getAllTranslations() {
        return translationRepository.findAll();
    }

    public List<Translation> getTranslationsBySegmentId(Long segmentId) {
        return translationRepository.findBySegmentId(segmentId);
    }

    public Optional<Translation> getTranslationById(Long id) {
        return Optional.ofNullable(translationRepository.findById(id));
    }

    public Translation createTranslation(Translation translation) {
        requireSegment(translation.getSegmentId());
        requireAuthor(translation.getAuthorId());
        translation.setId(null);
        if (translation.getStatus() == null) {
            translation.setStatus(DRAFT);
        }
        return translationRepository.save(translation);
    }

    public Translation updateTranslation(Long id, Translation details) {
        Translation translation = translationRepository.findById(id);
        if (translation == null) {
            throw BusinessException.notFound("Translation", id);
        }
        if (details.getSegmentId() != null) {
            requireSegment(details.getSegmentId());
            translation.setSegmentId(details.getSegmentId());
        }
        if (details.getAuthorId() != null) {
            requireAuthor(details.getAuthorId());
            translation.setAuthorId(details.getAuthorId());
        }
        if (details.getText() != null) {
            if (details.getText().isBlank()) throw BusinessException.badRequest("text must not be blank");
            translation.setText(details.getText());
        }
        if (details.getStatus() != null) {
            translation.setStatus(details.getStatus());
        }
        translation.setUpdatedAt(LocalDateTime.now());
        return translationRepository.save(translation);
    }

    /** Удаляет перевод вместе с его комментариями. */
    public void deleteTranslation(Long id) {
        if (!translationRepository.existsById(id)) {
            throw BusinessException.notFound("Translation", id);
        }
        commentRepository.deleteByTranslationId(id);
        translationRepository.deleteById(id);
    }

    public boolean translationExists(Long id) {
        return translationRepository.existsById(id);
    }

    private void requireSegment(Long segmentId) {
        if (!segmentRepository.existsById(segmentId)) {
            throw BusinessException.badRequest("Segment not found: " + segmentId);
        }
    }

    private void requireAuthor(Long authorId) {
        if (!userRepository.existsById(authorId)) {
            throw BusinessException.badRequest("Author not found: " + authorId);
        }
    }
}
