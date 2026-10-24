package org.example.repository;

import org.example.model.Translation;
import org.example.model.TranslationStatus;
import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {

    boolean existsByAuthor(User author);

    boolean existsBySegmentIdAndStatus(Long segmentId, TranslationStatus status);

    boolean existsBySegmentIdAndStatusIn(Long segmentId, Collection<TranslationStatus> statuses);

    @Query("""
            SELECT DISTINCT t FROM Translation t
            JOIN FETCH t.segment s
            JOIN FETCH s.project
            JOIN FETCH t.author
            """)
    List<Translation> findAllWithRelations();

    @Query("""
            SELECT t FROM Translation t
            JOIN FETCH t.segment s
            JOIN FETCH s.project
            JOIN FETCH t.author
            WHERE t.id = :id
            """)
    Optional<Translation> findByIdWithRelations(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT t FROM Translation t
            JOIN FETCH t.segment s
            JOIN FETCH s.project
            JOIN FETCH t.author
            WHERE s.id = :segmentId
            """)
    List<Translation> findBySegmentId(@Param("segmentId") Long segmentId);

    @Query("""
            SELECT t FROM Translation t
            JOIN FETCH t.segment s
            JOIN FETCH s.project
            JOIN FETCH t.author
            LEFT JOIN FETCH t.comments
            WHERE t.id = :id
            """)
    Optional<Translation> findByIdWithComments(@Param("id") Long id);
}
