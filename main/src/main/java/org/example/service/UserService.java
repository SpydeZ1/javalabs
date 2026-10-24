package org.example.service;

import org.example.dto.UserUpdateRequest;
import org.example.exception.BadRequestException;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.repository.TranslationProjectRepository;
import org.example.repository.TranslationRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TranslationProjectRepository projectRepository;
    private final TranslationRepository translationRepository;

    public UserService(UserRepository userRepository,
                       TranslationProjectRepository projectRepository,
                       TranslationRepository translationRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.translationRepository = translationRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    public User createUser(User user) {
        user.setUsername(user.getUsername().trim());
        user.setEmail(normalizeEmail(user.getEmail()));
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ConflictException("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email уже занят");
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, UserUpdateRequest request) {
        User user = getUserById(id);
        String username = request.getUsername() != null ? request.getUsername().trim() : user.getUsername();
        String email = request.getEmail() != null ? normalizeEmail(request.getEmail()) : user.getEmail();
        if (username.isBlank()) {
            throw new BadRequestException("Имя пользователя не может быть пустым");
        }
        if (email.isBlank()) {
            throw new BadRequestException("Email не может быть пустым");
        }
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new ConflictException("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("Email уже занят");
        }
        user.setUsername(username);
        user.setEmail(email);
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        return userRepository.saveAndFlush(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (projectRepository.existsByOwner(user) || translationRepository.existsByAuthor(user)) {
            throw new ConflictException("Нельзя удалить пользователя, пока на него ссылаются проекты или переводы");
        }
        userRepository.delete(user);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
