package org.example.repository;

import org.example.model.SourceSegment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class SourceSegmentRepository {
    private final ConcurrentHashMap<Long, SourceSegment> segments = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<SourceSegment> findAll() {
        return segments.values().stream().toList();
    }

    public List<SourceSegment> findByProjectId(Long projectId) {
        return segments.values().stream()
                .filter(segment -> projectId.equals(segment.getProjectId()))
                .collect(Collectors.toList());
    }

    public SourceSegment findById(Long id) {
        return segments.get(id);
    }

    public SourceSegment save(SourceSegment segment) {
        if (segment.getId() == null) {
            segment.setId(idGenerator.incrementAndGet());
        }
        segments.put(segment.getId(), segment);
        return segment;
    }

    public void deleteById(Long id) {
        segments.remove(id);
    }

    public boolean existsById(Long id) {
        return segments.containsKey(id);
    }
}