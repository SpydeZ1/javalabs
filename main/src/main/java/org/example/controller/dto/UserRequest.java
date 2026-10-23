package org.example.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.model.UserRole;

/** Тело POST/PATCH. При PATCH не переданные (null) поля не меняются. id и время клиент задать не может. */
public record UserRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        UserRole role) {
}
