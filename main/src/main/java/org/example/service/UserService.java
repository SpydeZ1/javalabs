package org.example.service;

import org.example.exception.BusinessException;
import org.example.model.Translation;
import org.example.model.TranslationProject;
import org.example.model.User;
import org.example.model.UserRole;
import org.example.repository.TranslationProjectRepository;
import org.example.repository.TranslationRepository;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TranslationProjectRepository projectRepository;
    private final TranslationRepository translationRepository;
    private final TranslationProjectService projectService;
    private final TranslationService translationService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       TranslationProjectRepository projectRepository,
                       TranslationRepository translationRepository,
                       TranslationProjectService projectService,
                       TranslationService translationService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.translationRepository = translationRepository;
        this.projectService = projectService;
        this.translationService = translationService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(userRepository.findById(id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // synchronized: проверка уникальности и сохранение должны быть одной атомарной операцией
    public synchronized User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw BusinessException.conflict("Username already exists: " + user.getUsername());
        }
        user.setId(null);
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public synchronized User updateUser(Long id, User details) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw BusinessException.notFound("User", id);
        }
        if (details.getUsername() != null) {
            requireNotBlank("username", details.getUsername());
            User other = userRepository.findByUsername(details.getUsername());
            if (other != null && !other.getId().equals(id)) {
                throw BusinessException.conflict("Username already exists: " + details.getUsername());
            }
            user.setUsername(details.getUsername());
        }
        if (details.getEmail() != null) {
            requireNotBlank("email", details.getEmail());
            user.setEmail(details.getEmail());
        }
        if (details.getPassword() != null) {
            requireNotBlank("password", details.getPassword());
            user.setPassword(passwordEncoder.encode(details.getPassword()));
        }
        if (details.getRole() != null) {
            user.setRole(details.getRole());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw BusinessException.notFound("User", id);
        }
        // Каскад: проекты пользователя (с сегментами, переводами, комментариями),
        // затем его переводы в чужих проектах (с комментариями). Висячих ссылок не остаётся.
        for (TranslationProject project : projectRepository.findByOwnerId(id)) {
            projectService.deleteProject(project.getId());
        }
        for (Translation translation : translationRepository.findByAuthorId(id)) {
            translationService.deleteTranslation(translation.getId());
        }
        userRepository.deleteById(id);
    }

    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    private static void requireNotBlank(String field, String value) {
        if (value.isBlank()) {
            throw BusinessException.badRequest(field + " must not be blank");
        }
    }
}
