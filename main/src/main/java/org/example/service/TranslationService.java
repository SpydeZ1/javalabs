package org.example.service;

import org.example.model.Translation;
import org.example.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TranslationService {

    @Autowired
    private TranslationRepository translationRepository;

    public List<Translation> getAllTranslations() {
        return translationRepository.findAll();
    }

    public List<Translation> getTranslationsBySegmentId(Long segmentId) {
        return translationRepository.findBySegmentId(segmentId);
    }

    public Optional<Translation> getTranslationById(Long id) {
        return Optional.ofNullable(translationRepository.findById(id));
    }

    public Translation createTranslation(Translation translation) {
        return translationRepository.save(translation);
    }

    public Translation updateTranslation(Long id, Translation translationDetails) {
        Translation translation = translationRepository.findById(id);
        if (translation != null) {
            translation.setSegmentId(translationDetails.getSegmentId());
            translation.setAuthor(translationDetails.getAuthor());
            translation.setText(translationDetails.getText());
            translation.setStatus(translationDetails.getStatus());
            return translationRepository.save(translation);
        }
        return null;
    }

    public void deleteTranslation(Long id) {
        translationRepository.deleteById(id);
    }

    public boolean translationExists(Long id) {
        return translationRepository.existsById(id);
    }
}