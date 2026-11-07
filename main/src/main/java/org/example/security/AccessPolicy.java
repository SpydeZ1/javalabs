package org.example.security;

import org.example.repository.EditorialCommentRepository;
import org.example.repository.SourceSegmentRepository;
import org.example.repository.TranslationProjectRepository;
import org.example.repository.TranslationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("access")
@Transactional(readOnly = true)
public class AccessPolicy {

    private final TranslationProjectRepository projectRepository;
    private final SourceSegmentRepository segmentRepository;
    private final TranslationRepository translationRepository;
    private final EditorialCommentRepository commentRepository;

    public AccessPolicy(TranslationProjectRepository projectRepository,
                        SourceSegmentRepository segmentRepository,
                        TranslationRepository translationRepository,
                        EditorialCommentRepository commentRepository) {
        this.projectRepository = projectRepository;
        this.segmentRepository = segmentRepository;
        this.translationRepository = translationRepository;
        this.commentRepository = commentRepository;
    }

    public boolean ownsProject(Long projectId, Long userId) {
        return projectId == null
                || !projectRepository.existsById(projectId)
                || projectRepository.existsByIdAndOwnerId(projectId, userId);
    }

    public boolean ownsSegmentProject(Long segmentId, Long userId) {
        return segmentId == null
                || !segmentRepository.existsById(segmentId)
                || segmentRepository.existsByIdAndProjectOwnerId(segmentId, userId);
    }

    public boolean isAuthor(Long translationId, Long userId) {
        return translationId != null && translationRepository.existsByIdAndAuthorId(translationId, userId);
    }

    public boolean isAuthorOrMissing(Long translationId, Long userId) {
        return translationId == null
                || !translationRepository.existsById(translationId)
                || translationRepository.existsByIdAndAuthorId(translationId, userId);
    }

    public boolean isAuthorOfCommented(Long commentId, Long userId) {
        return commentId != null && commentRepository.existsByIdAndTranslationAuthorId(commentId, userId);
    }

    public boolean isAuthorOfCommentedOrMissing(Long commentId, Long userId) {
        return commentId == null
                || !commentRepository.existsById(commentId)
                || commentRepository.existsByIdAndTranslationAuthorId(commentId, userId);
    }
}
