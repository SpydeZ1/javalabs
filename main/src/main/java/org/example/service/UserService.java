package org.example.service;

import org.example.dto.UserUpdateRequest;
import org.example.exception.BadRequestException;
import org.example.exception.ConflictException;
import org.example.exception.NotFoundException;
import org.example.model.User;
import org.example.model.UserRole;
import org.example.model.UserStatus;
import org.example.repository.TranslationProjectRepository;
import org.example.repository.TranslationRepository;
import org.example.repository.UserRepository;
import org.example.security.AppUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       TranslationProjectRepository projectRepository,
                       TranslationRepository translationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.translationRepository = translationRepository;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional(readOnly = true)
    public User getActiveUserById(Long id) {
        User user = getUserById(id);
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ConflictException("Пользователь заблокирован: " + id);
        }
        return user;
    }

    public User createUser(User user, String rawPassword) {
        user.setUsername(user.getUsername().trim());
        user.setEmail(normalizeEmail(user.getEmail()));
        if (rawPassword.toLowerCase(Locale.ROOT).contains(user.getUsername().toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Пароль не должен содержать имя пользователя");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ConflictException("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email уже занят");
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public User register(User user, String rawPassword) {
        user.setRole(UserRole.TRANSLATOR);
        return createUser(user, rawPassword);
    }

    public User updateUser(Long id, UserUpdateRequest request, AppUserDetails actor) {
        boolean admin = actor.getRole() == UserRole.ADMIN;
        boolean self = actor.getId().equals(id);
        if (!admin && !self) {
            throw new AccessDeniedException("Недостаточно прав");
        }
        User user = getUserById(id);
        boolean roleChanges = request.getRole() != null && request.getRole() != user.getRole();
        boolean statusChanges = request.getStatus() != null && request.getStatus() != user.getStatus();
        if (roleChanges || statusChanges) {
            if (!admin) {
                throw new AccessDeniedException("Недостаточно прав");
            }
            if (self) {
                throw new ConflictException("Администратор не может менять собственные роль и статус");
            }
        }
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
        if (roleChanges) {
            user.setRole(request.getRole());
        }
        if (statusChanges) {
            user.setStatus(request.getStatus());
        }
        return userRepository.saveAndFlush(user);
    }

    public void deleteUser(Long id, AppUserDetails actor) {
        User user = getUserById(id);
        if (actor.getId().equals(id)) {
            throw new ConflictException("Нельзя удалить собственную учётную запись");
        }
        if (projectRepository.existsByOwner(user) || translationRepository.existsByAuthor(user)) {
            throw new ConflictException("Нельзя удалить пользователя, пока на него ссылаются проекты или переводы");
        }
        userRepository.delete(user);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
