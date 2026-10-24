package org.example.repository;

import org.example.model.TranslationProject;
import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationProjectRepository extends JpaRepository<TranslationProject, Long> {

    boolean existsByOwner(User owner);

    @Query("SELECT DISTINCT p FROM TranslationProject p JOIN FETCH p.owner")
    List<TranslationProject> findAllWithOwner();

    @Query("SELECT p FROM TranslationProject p JOIN FETCH p.owner WHERE p.id = :id")
    Optional<TranslationProject> findByIdWithOwner(@Param("id") Long id);
}
