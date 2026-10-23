package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.SourceSegmentRequest;
import org.example.controller.dto.SourceSegmentResponse;
import org.example.service.SourceSegmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/source-segments")
public class SourceSegmentController {

    private final SourceSegmentService service;

    public SourceSegmentController(SourceSegmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<SourceSegmentResponse> getAll() {
        return service.getAllSegments().stream().map(Mappers::toSegmentResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceSegmentResponse> getById(@PathVariable Long id) {
        return service.getSegmentById(id)
                .map(Mappers::toSegmentResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}")
    public List<SourceSegmentResponse> getByProjectId(@PathVariable Long projectId) {
        return service.getSegmentsByProjectId(projectId).stream().map(Mappers::toSegmentResponse).toList();
    }

    @PostMapping
    public ResponseEntity<SourceSegmentResponse> create(@Valid @RequestBody SourceSegmentRequest body) {
        var created = service.createSegment(Mappers.toSegment(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(Mappers.toSegmentResponse(created));
    }

    /** Частичное обновление: поля, не переданные в теле, остаются без изменений. */
    @PatchMapping("/{id}")
    public SourceSegmentResponse update(@PathVariable Long id, @RequestBody SourceSegmentRequest body) {
        return Mappers.toSegmentResponse(service.updateSegment(id, Mappers.toSegment(body)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteSegment(id);
        return ResponseEntity.noContent().build();
    }
}
