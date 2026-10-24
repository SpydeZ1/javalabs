package org.example.repository;

import org.example.model.SourceSegment;
import org.example.model.TranslationProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceSegmentRepository extends JpaRepository<SourceSegment, Long> {

    boolean existsByProjectAndSequenceNumber(TranslationProject project, Integer sequenceNumber);

    boolean existsByProjectAndSequenceNumberAndIdNot(TranslationProject project, Integer sequenceNumber, Long id);

    long countByProject(TranslationProject project);

    @Query("""
            SELECT COUNT(s) FROM SourceSegment s
            WHERE s.project.id = :projectId
              AND NOT EXISTS (
                SELECT 1 FROM Translation t
                WHERE t.segment = s AND t.status = org.example.model.TranslationStatus.APPROVED)
            """)
    long countWithoutApprovedTranslation(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT s FROM SourceSegment s JOIN FETCH s.project p JOIN FETCH p.owner")
    List<SourceSegment> findAllWithProject();

    @Query("SELECT s FROM SourceSegment s JOIN FETCH s.project p JOIN FETCH p.owner WHERE s.id = :id")
    Optional<SourceSegment> findByIdWithProject(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM SourceSegment s JOIN FETCH s.project p JOIN FETCH p.owner WHERE p.id = :projectId")
    List<SourceSegment> findByProjectId(@Param("projectId") Long projectId);
}
