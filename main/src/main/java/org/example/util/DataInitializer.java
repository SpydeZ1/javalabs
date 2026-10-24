package org.example.util;

import org.example.model.CommentStatus;
import org.example.model.EditorialComment;
import org.example.model.ProjectStatus;
import org.example.model.SourceSegment;
import org.example.model.Translation;
import org.example.model.TranslationProject;
import org.example.model.TranslationStatus;
import org.example.model.User;
import org.example.model.UserRole;
import org.example.repository.EditorialCommentRepository;
import org.example.repository.SourceSegmentRepository;
import org.example.repository.TranslationProjectRepository;
import org.example.repository.TranslationRepository;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TranslationProjectRepository projectRepository;
    private final SourceSegmentRepository segmentRepository;
    private final TranslationRepository translationRepository;
    private final EditorialCommentRepository commentRepository;

    public DataInitializer(UserRepository userRepository,
                           TranslationProjectRepository projectRepository,
                           SourceSegmentRepository segmentRepository,
                           TranslationRepository translationRepository,
                           EditorialCommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.segmentRepository = segmentRepository;
        this.translationRepository = translationRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User anna = new User();
        anna.setUsername("anna");
        anna.setEmail("anna@studio.local");
        anna.setRole(UserRole.TRANSLATOR);
        userRepository.save(anna);

        User boris = new User();
        boris.setUsername("boris");
        boris.setEmail("boris@studio.local");
        boris.setRole(UserRole.EDITOR);
        userRepository.save(boris);

        TranslationProject draft = new TranslationProject();
        draft.setName("Интерфейс кабинета");
        draft.setTargetLanguage("en");
        draft.setOwner(anna);
        draft.setStatus(ProjectStatus.DRAFT);
        projectRepository.save(draft);

        TranslationProject active = new TranslationProject();
        active.setName("Письма поддержки");
        active.setTargetLanguage("de");
        active.setOwner(anna);
        active.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(active);

        SourceSegment draftSegment = new SourceSegment();
        draftSegment.setProject(draft);
        draftSegment.setContent("Сохранить изменения");
        draftSegment.setSequenceNumber(1);
        segmentRepository.save(draftSegment);

        SourceSegment first = new SourceSegment();
        first.setProject(active);
        first.setContent("Ваш запрос принят в работу.");
        first.setSequenceNumber(1);
        segmentRepository.save(first);

        SourceSegment second = new SourceSegment();
        second.setProject(active);
        second.setContent("Мы ответим в течение суток.");
        second.setSequenceNumber(2);
        segmentRepository.save(second);

        Translation rejected = new Translation();
        rejected.setSegment(first);
        rejected.setAuthor(anna);
        rejected.setText("Your request has been received.");
        rejected.setStatus(TranslationStatus.REJECTED);
        translationRepository.save(rejected);

        Translation draftTranslation = new Translation();
        draftTranslation.setSegment(second);
        draftTranslation.setAuthor(anna);
        draftTranslation.setText("We will reply within a day.");
        draftTranslation.setStatus(TranslationStatus.DRAFT);
        translationRepository.save(draftTranslation);

        EditorialComment comment = new EditorialComment();
        comment.setTranslation(rejected);
        comment.setContent("Лучше: Your request is being processed.");
        comment.setStatus(CommentStatus.OPEN);
        commentRepository.save(comment);
    }
}
