package org.example.repository;

import org.example.model.SourceSegment;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
public class SourceSegmentRepository extends InMemoryRepository<SourceSegment> {

    public SourceSegmentRepository() {
        super(SourceSegment::getId, SourceSegment::setId);
    }

    /** Сегменты проекта в порядке поля order. */
    public List<SourceSegment> findByProjectId(Long projectId) {
        return stream()
                .filter(s -> s.getProjectId() != null && s.getProjectId().equals(projectId))
                .sorted(Comparator.comparing(SourceSegment::getOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SourceSegment::getId))
                .toList();
    }
}
