package org.example.repository;

import org.example.model.Translation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TranslationRepository {
    private final ConcurrentHashMap<Long, Translation> translations = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<Translation> findAll() {
        return translations.values().stream().toList();
    }

    public List<Translation> findBySegmentId(Long segmentId) {
        return translations.values().stream()
                .filter(translation -> segmentId.equals(translation.getSegmentId()))
                .collect(Collectors.toList());
    }

    public Translation findById(Long id) {
        return translations.get(id);
    }

    public Translation save(Translation translation) {
        if (translation.getId() == null) {
            translation.setId(idGenerator.incrementAndGet());
        }
        translations.put(translation.getId(), translation);
        return translation;
    }

    public void deleteById(Long id) {
        translations.remove(id);
    }

    public boolean existsById(Long id) {
        return translations.containsKey(id);
    }
}