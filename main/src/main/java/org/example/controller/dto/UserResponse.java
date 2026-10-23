package org.example.controller.dto;

import org.example.model.UserRole;

import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String email, UserRole role,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
}
