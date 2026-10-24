package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.SourceSegmentRequest;
import org.example.dto.SourceSegmentResponse;
import org.example.dto.SourceSegmentUpdateRequest;
import org.example.mapper.SourceSegmentMapper;
import org.example.model.SourceSegment;
import org.example.service.SourceSegmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/source-segments")
public class SourceSegmentController {

    private final SourceSegmentService segmentService;

    public SourceSegmentController(SourceSegmentService segmentService) {
        this.segmentService = segmentService;
    }

    @GetMapping
    public List<SourceSegmentResponse> getAllSegments() {
        return segmentService.getAllSegments().stream()
                .map(SourceSegmentMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public SourceSegmentResponse getSegmentById(@PathVariable Long id) {
        return SourceSegmentMapper.toResponse(segmentService.getSegmentById(id));
    }

    @GetMapping("/project/{projectId}")
    public List<SourceSegmentResponse> getSegmentsByProjectId(@PathVariable Long projectId) {
        return segmentService.getSegmentsByProjectId(projectId).stream()
                .map(SourceSegmentMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<SourceSegmentResponse> createSegment(@Valid @RequestBody SourceSegmentRequest request) {
        SourceSegment created = segmentService.createSegment(
                SourceSegmentMapper.toEntity(request), request.getProjectId());
        return ResponseEntity.status(HttpStatus.CREATED).body(SourceSegmentMapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public SourceSegmentResponse updateSegment(
            @PathVariable Long id,
            @Valid @RequestBody SourceSegmentUpdateRequest request) {
        return SourceSegmentMapper.toResponse(segmentService.updateSegment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSegment(@PathVariable Long id) {
        segmentService.deleteSegment(id);
        return ResponseEntity.noContent().build();
    }
}
