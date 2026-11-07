package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.model.UserRole;
import org.example.validation.StrongPassword;

public class UserRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,64}$", message = "допустимы 3-64 символа: латинские буквы, цифры, точка, дефис и подчёркивание")
    private String username;

    @Email
    @NotBlank
    @Size(max = 255)
    private String email;

    @NotBlank
    @StrongPassword
    private String password;

    @NotNull
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
