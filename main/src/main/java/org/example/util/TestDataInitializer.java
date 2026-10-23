package org.example.util;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private TranslationProjectRepository projectRepository;

    @Autowired
    private SourceSegmentRepository segmentRepository;

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private EditorialCommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create test users
        User user1 = new User(null, "john_doe", "john@example.com", "password123", UserRole.USER);
        User user2 = new User(null, "editor_smith", "editor@example.com", "password123", UserRole.EDITOR);
        User admin = new User(null, "admin_user", "admin@example.com", "password123", UserRole.ADMIN);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(admin);

        // Create test projects
        TranslationProject project1 = new TranslationProject(null, "Project Alpha", "French", user1.getId(), ProjectStatus.IN_PROGRESS);
        TranslationProject project2 = new TranslationProject(null, "Project Beta", "Spanish", user2.getId(), ProjectStatus.DRAFT);

        projectRepository.save(project1);
        projectRepository.save(project2);

        // Create source segments
        SourceSegment segment1 = new SourceSegment(null, project1.getId(), "Hello world!", 1);
        SourceSegment segment2 = new SourceSegment(null, project1.getId(), "Good morning.", 2);
        SourceSegment segment3 = new SourceSegment(null, project2.getId(), "Welcome to our application.", 1);

        segmentRepository.save(segment1);
        segmentRepository.save(segment2);
        segment3.setProjectId(project2.getId());
        segmentRepository.save(segment3);

        // Create translations
        Translation translation1 = new Translation(null, segment1.getId(), user1.getId(), "Bonjour le monde!", TranslationStatus.APPROVED);
        Translation translation2 = new Translation(null, segment2.getId(), user2.getId(), "Bonjour matin.", TranslationStatus.SUBMITTED);
        Translation translation3 = new Translation(null, segment3.getId(), admin.getId(), "Bienvenue dans notre application.", TranslationStatus.DRAFT);

        translationRepository.save(translation1);
        translationRepository.save(translation2);
        translationRepository.save(translation3);

        // Create editorial comments
        EditorialComment comment1 = new EditorialComment(null, translation1.getId(), "Good translation!", CommentStatus.RESOLVED);
        EditorialComment comment2 = new EditorialComment(null, translation2.getId(), "Consider rephrasing.", CommentStatus.OPEN);

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        System.out.println("Test data initialized successfully!");
    }
}