package org.example.service;

import org.example.exception.BusinessException;
import org.example.model.SourceSegment;
import org.example.model.Translation;
import org.example.repository.SourceSegmentRepository;
import org.example.repository.TranslationProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SourceSegmentService {

    private final SourceSegmentRepository segmentRepository;
    private final TranslationProjectRepository projectRepository;
    private final TranslationService translationService;

    public SourceSegmentService(SourceSegmentRepository segmentRepository,
                                TranslationProjectRepository projectRepository,
                                TranslationService translationService) {
        this.segmentRepository = segmentRepository;
        this.projectRepository = projectRepository;
        this.translationService = translationService;
    }

    public List<SourceSegment> getAllSegments() {
        return segmentRepository.findAll();
    }

    /** Сегменты проекта, отсортированные по order. */
    public List<SourceSegment> getSegmentsByProjectId(Long projectId) {
        return segmentRepository.findByProjectId(projectId);
    }

    public Optional<SourceSegment> getSegmentById(Long id) {
        return Optional.ofNullable(segmentRepository.findById(id));
    }

    public SourceSegment createSegment(SourceSegment segment) {
        requireProject(segment.getProjectId());
        segment.setId(null);
        return segmentRepository.save(segment);
    }

    public SourceSegment updateSegment(Long id, SourceSegment details) {
        SourceSegment segment = segmentRepository.findById(id);
        if (segment == null) {
            throw BusinessException.notFound("Segment", id);
        }
        if (details.getProjectId() != null) {
            requireProject(details.getProjectId());
            segment.setProjectId(details.getProjectId());
        }
        if (details.getSourceText() != null) {
            if (details.getSourceText().isBlank()) throw BusinessException.badRequest("sourceText must not be blank");
            segment.setSourceText(details.getSourceText());
        }
        if (details.getOrder() != null) {
            if (details.getOrder() < 1) throw BusinessException.badRequest("order must be >= 1");
            segment.setOrder(details.getOrder());
        }
        segment.setUpdatedAt(LocalDateTime.now());
        return segmentRepository.save(segment);
    }

    /** Удаляет сегмент вместе с его переводами и комментариями. */
    public void deleteSegment(Long id) {
        if (!segmentRepository.existsById(id)) {
            throw BusinessException.notFound("Segment", id);
        }
        for (Translation t : translationService.getTranslationsBySegmentId(id)) {
            translationService.deleteTranslation(t.getId());
        }
        segmentRepository.deleteById(id);
    }

    public boolean segmentExists(Long id) {
        return segmentRepository.existsById(id);
    }

    private void requireProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw BusinessException.badRequest("Project not found: " + projectId);
        }
    }
}
