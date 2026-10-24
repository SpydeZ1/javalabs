package org.example.repository;

import org.example.model.CommentStatus;
import org.example.model.EditorialComment;
import org.example.model.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EditorialCommentRepository extends JpaRepository<EditorialComment, Long> {

    boolean existsByTranslationAndStatus(Translation translation, CommentStatus status);

    @Query("""
            SELECT DISTINCT c FROM EditorialComment c
            JOIN FETCH c.translation t
            JOIN FETCH t.segment s
            JOIN FETCH s.project
            JOIN FETCH t.author
            """)
    List<EditorialComment> findAllWithTranslation();

    @Query("""
            SELECT c FROM EditorialComment c
            JOIN FETCH c.translation t
            JOIN FETCH t.segment s
            JOIN FETCH s.project
            JOIN FETCH t.author
            WHERE c.id = :id
            """)
    Optional<EditorialComment> findByIdWithTranslation(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT c FROM EditorialComment c
            JOIN FETCH c.translation t
            JOIN FETCH t.segment s
            JOIN FETCH s.project
            JOIN FETCH t.author
            WHERE t.id = :translationId
            """)
    List<EditorialComment> findByTranslationId(@Param("translationId") Long translationId);
}
