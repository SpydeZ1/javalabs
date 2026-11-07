package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.model.UserRole;
import org.example.model.UserStatus;

public class UserUpdateRequest {

    @Pattern(regexp = "^[A-Za-z0-9._-]{3,64}$", message = "допустимы 3-64 символа: латинские буквы, цифры, точка, дефис и подчёркивание")
    private String username;

    @Email
    @Size(max = 255)
    private String email;

    private UserRole role;

    private UserStatus status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
