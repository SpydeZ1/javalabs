package org.example.dto;

import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Email;
import org.example.model.UserRole;

public class UserUpdateRequest {

    @Size(max = 64)
    private String username;

    @Email
    @Size(max = 255)
    private String email;

    private UserRole role;

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
}
