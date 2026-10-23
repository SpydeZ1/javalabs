package org.example.repository;

import org.example.model.TranslationProject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TranslationProjectRepository {
    private final ConcurrentHashMap<Long, TranslationProject> projects = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<TranslationProject> findAll() {
        return projects.values().stream().toList();
    }

    public TranslationProject findById(Long id) {
        return projects.get(id);
    }

    public TranslationProject save(TranslationProject project) {
        if (project.getId() == null) {
            project.setId(idGenerator.incrementAndGet());
        }
        projects.put(project.getId(), project);
        return project;
    }

    public void deleteById(Long id) {
        projects.remove(id);
    }

    public boolean existsById(Long id) {
        return projects.containsKey(id);
    }
}