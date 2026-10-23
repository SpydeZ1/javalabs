package org.example.repository;

import org.example.model.Translation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TranslationRepository extends InMemoryRepository<Translation> {

    public TranslationRepository() {
        super(Translation::getId, Translation::setId);
    }

    public List<Translation> findBySegmentId(Long segmentId) {
        return stream().filter(t -> t.getSegmentId() != null && t.getSegmentId().equals(segmentId)).toList();
    }

    public List<Translation> findByAuthorId(Long authorId) {
        return stream().filter(t -> t.getAuthorId() != null && t.getAuthorId().equals(authorId)).toList();
    }
}
