package org.example.repository;

import org.example.model.EditorialComment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EditorialCommentRepository extends InMemoryRepository<EditorialComment> {

    public EditorialCommentRepository() {
        super(EditorialComment::getId, EditorialComment::setId);
    }

    public List<EditorialComment> findByTranslationId(Long translationId) {
        return stream()
                .filter(c -> c.getTranslationId() != null && c.getTranslationId().equals(translationId))
                .toList();
    }

    public void deleteByTranslationId(Long translationId) {
        findByTranslationId(translationId).forEach(c -> deleteById(c.getId()));
    }
}
