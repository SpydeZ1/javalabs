package org.example.util;

import org.example.model.User;
import org.example.model.UserRole;
import org.example.model.UserStatus;
import org.example.repository.UserRepository;
import org.example.validation.StrongPasswordValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Order(2)
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.admin.username:admin}") String username,
                          @Value("${app.admin.email:admin@studio.local}") String email,
                          @Value("${app.admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (password == null || password.isBlank() || userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }
        if (!StrongPasswordValidator.isStrong(password)) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD не соответствует требованиям к надёжности пароля");
        }
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Имя или email начального администратора уже заняты другим пользователем");
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email.toLowerCase(Locale.ROOT));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(admin);
    }
}
