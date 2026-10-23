package org.example.controller;

import org.example.model.SourceSegment;
import org.example.service.SourceSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/source-segments")
public class SourceSegmentController {

    @Autowired
    private SourceSegmentService segmentService;

    @GetMapping
    public List<SourceSegment> getAllSegments() {
        return segmentService.getAllSegments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceSegment> getSegmentById(@PathVariable Long id) {
        Optional<SourceSegment> segment = segmentService.getSegmentById(id);
        return segment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}")
    public List<SourceSegment> getSegmentsByProjectId(@PathVariable Long projectId) {
        return segmentService.getSegmentsByProjectId(projectId);
    }

    @PostMapping
    public ResponseEntity<SourceSegment> createSegment(@RequestBody SourceSegment segment) {
        SourceSegment createdSegment = segmentService.createSegment(segment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSegment);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SourceSegment> updateSegment(@PathVariable Long id, @RequestBody SourceSegment segmentDetails) {
        SourceSegment updatedSegment = segmentService.updateSegment(id, segmentDetails);
        if (updatedSegment != null) {
            return ResponseEntity.ok(updatedSegment);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSegment(@PathVariable Long id) {
        segmentService.deleteSegment(id);
        return ResponseEntity.noContent().build();
    }
}