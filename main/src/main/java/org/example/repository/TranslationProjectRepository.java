package org.example.repository;

import org.example.model.TranslationProject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TranslationProjectRepository extends InMemoryRepository<TranslationProject> {

    public TranslationProjectRepository() {
        super(TranslationProject::getId, TranslationProject::setId);
    }

    public List<TranslationProject> findByOwnerId(Long ownerId) {
        return stream().filter(p -> p.getOwnerId() != null && p.getOwnerId().equals(ownerId)).toList();
    }
}
