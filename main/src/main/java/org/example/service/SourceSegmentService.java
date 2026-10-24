package org.example.service;

import org.example.dto.SourceSegmentUpdateRequest;
import org.example.exception.BadRequestException;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.mapper.SourceSegmentMapper;
import org.example.model.SourceSegment;
import org.example.model.TranslationProject;
import org.example.model.TranslationStatus;
import org.example.repository.SourceSegmentRepository;
import org.example.repository.TranslationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SourceSegmentService {

    private static final List<TranslationStatus> LOCKING_STATUSES =
            List.of(TranslationStatus.SUBMITTED, TranslationStatus.APPROVED);

    private final SourceSegmentRepository segmentRepository;
    private final TranslationRepository translationRepository;
    private final TranslationProjectService projectService;

    public SourceSegmentService(SourceSegmentRepository segmentRepository,
                                TranslationRepository translationRepository,
                                TranslationProjectService projectService) {
        this.segmentRepository = segmentRepository;
        this.translationRepository = translationRepository;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<SourceSegment> getAllSegments() {
        return segmentRepository.findAllWithProject();
    }

    @Transactional(readOnly = true)
    public List<SourceSegment> getSegmentsByProjectId(Long projectId) {
        projectService.getProjectById(projectId);
        return segmentRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public SourceSegment getSegmentById(Long id) {
        return segmentRepository.findByIdWithProject(id)
                .orElseThrow(() -> new NotFoundException("Сегмент не найден: " + id));
    }

    public SourceSegment createSegment(SourceSegment segment, Long projectId) {
        TranslationProject project = projectService.getProjectById(projectId);
        projectService.assertEditable(project);
        if (segmentRepository.existsByProjectAndSequenceNumber(project, segment.getSequenceNumber())) {
            throw new ConflictException("В проекте уже есть сегмент с таким порядковым номером");
        }
        segment.setProject(project);
        return segmentRepository.save(segment);
    }

    public SourceSegment updateSegment(Long id, SourceSegmentUpdateRequest request) {
        SourceSegment segment = getSegmentById(id);
        projectService.assertEditable(segment.getProject());
        if (request.getContent() != null) {
            if (request.getContent().isBlank()) {
                throw new BadRequestException("Текст сегмента не может быть пустым");
            }
            if (!request.getContent().equals(segment.getContent())) {
                assertNoLockingTranslations(segment);
            }
        }
        Integer sequenceNumber = request.getSequenceNumber() != null
                ? request.getSequenceNumber()
                : segment.getSequenceNumber();
        if (segmentRepository.existsByProjectAndSequenceNumberAndIdNot(segment.getProject(), sequenceNumber, id)) {
            throw new ConflictException("В проекте уже есть сегмент с таким порядковым номером");
        }
        SourceSegmentMapper.apply(request, segment);
        return segmentRepository.saveAndFlush(segment);
    }

    public void deleteSegment(Long id) {
        SourceSegment segment = getSegmentById(id);
        projectService.assertEditable(segment.getProject());
        assertNoLockingTranslations(segment);
        segmentRepository.delete(segment);
    }

    private void assertNoLockingTranslations(SourceSegment segment) {
        if (translationRepository.existsBySegmentIdAndStatusIn(segment.getId(), LOCKING_STATUSES)) {
            throw new ConflictException(
                    "Сегмент нельзя менять или удалять: у него есть перевод на проверке или утверждённый");
        }
    }
}
