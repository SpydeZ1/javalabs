package org.example.service;

import org.example.model.SourceSegment;
import org.example.repository.SourceSegmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SourceSegmentService {

    @Autowired
    private SourceSegmentRepository segmentRepository;

    public List<SourceSegment> getAllSegments() {
        return segmentRepository.findAll();
    }

    public List<SourceSegment> getSegmentsByProjectId(Long projectId) {
        return segmentRepository.findByProjectId(projectId);
    }

    public Optional<SourceSegment> getSegmentById(Long id) {
        return Optional.ofNullable(segmentRepository.findById(id));
    }

    public SourceSegment createSegment(SourceSegment segment) {
        return segmentRepository.save(segment);
    }

    public SourceSegment updateSegment(Long id, SourceSegment segmentDetails) {
        SourceSegment segment = segmentRepository.findById(id);
        if (segment != null) {
            segment.setProjectId(segmentDetails.getProjectId());
            segment.setSourceText(segmentDetails.getSourceText());
            segment.setOrder(segmentDetails.getOrder());
            return segmentRepository.save(segment);
        }
        return null;
    }

    public void deleteSegment(Long id) {
        segmentRepository.deleteById(id);
    }

    public boolean segmentExists(Long id) {
        return segmentRepository.existsById(id);
    }
}