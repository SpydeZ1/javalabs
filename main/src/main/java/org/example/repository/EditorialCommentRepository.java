package org.example.repository;

import org.example.model.EditorialComment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class EditorialCommentRepository {
    private final ConcurrentHashMap<Long, EditorialComment> comments = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<EditorialComment> findAll() {
        return comments.values().stream().toList();
    }

    public List<EditorialComment> findByTranslationId(Long translationId) {
        return comments.values().stream()
                .filter(comment -> translationId.equals(comment.getTranslationId()))
                .collect(Collectors.toList());
    }

    public EditorialComment findById(Long id) {
        return comments.get(id);
    }

    public EditorialComment save(EditorialComment comment) {
        if (comment.getId() == null) {
            comment.setId(idGenerator.incrementAndGet());
        }
        comments.put(comment.getId(), comment);
        return comment;
    }

    public void deleteById(Long id) {
        comments.remove(id);
    }

    public boolean existsById(Long id) {
        return comments.containsKey(id);
    }
}