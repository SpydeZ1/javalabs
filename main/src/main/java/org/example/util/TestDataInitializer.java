package org.example.util;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Заполняет хранилище демо-данными. Включается свойством app.seed-data=true. */
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class TestDataInitializer implements CommandLineRunner {

    private final TranslationProjectRepository projectRepository;
    private final SourceSegmentRepository segmentRepository;
    private final TranslationRepository translationRepository;
    private final EditorialCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(TranslationProjectRepository projectRepository,
                               SourceSegmentRepository segmentRepository,
                               TranslationRepository translationRepository,
                               EditorialCommentRepository commentRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.projectRepository = projectRepository;
        this.segmentRepository = segmentRepository;
        this.translationRepository = translationRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String password = passwordEncoder.encode("password123");
        User user1 = userRepository.save(new User(null, "john_doe", "john@example.com", password, UserRole.USER));
        User user2 = userRepository.save(new User(null, "editor_smith", "editor@example.com", password, UserRole.EDITOR));
        User admin = userRepository.save(new User(null, "admin_user", "admin@example.com", password, UserRole.ADMIN));

        TranslationProject project1 = projectRepository.save(
                new TranslationProject(null, "Project Alpha", "French", user1.getId(), ProjectStatus.IN_PROGRESS));
        TranslationProject project2 = projectRepository.save(
                new TranslationProject(null, "Project Beta", "Spanish", user2.getId(), ProjectStatus.DRAFT));

        SourceSegment segment1 = segmentRepository.save(new SourceSegment(null, project1.getId(), "Hello world!", 1));
        SourceSegment segment2 = segmentRepository.save(new SourceSegment(null, project1.getId(), "Good morning.", 2));
        SourceSegment segment3 = segmentRepository.save(
                new SourceSegment(null, project2.getId(), "Welcome to our application.", 1));

        // Язык перевода соответствует targetLanguage проекта: Alpha - французский, Beta - испанский
        Translation translation1 = translationRepository.save(
                new Translation(null, segment1.getId(), user1.getId(), "Bonjour le monde !", TranslationStatus.APPROVED));
        Translation translation2 = translationRepository.save(
                new Translation(null, segment2.getId(), user2.getId(), "Bonjour.", TranslationStatus.SUBMITTED));
        Translation translation3 = translationRepository.save(
                new Translation(null, segment3.getId(), admin.getId(), "Bienvenido a nuestra aplicación.", TranslationStatus.DRAFT));

        commentRepository.save(new EditorialComment(null, translation1.getId(), "Good translation!", CommentStatus.RESOLVED));
        commentRepository.save(new EditorialComment(null, translation2.getId(), "Consider rephrasing.", CommentStatus.OPEN));
    }
}
