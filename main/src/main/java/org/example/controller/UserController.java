package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.dto.UserUpdateRequest;
import org.example.mapper.UserMapper;
import org.example.model.User;
import org.example.security.AppUserDetails;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal AppUserDetails actor) {
        return UserMapper.toResponse(userService.getUserById(actor.getId()));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return UserMapper.toResponse(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User created = userService.createUser(UserMapper.toEntity(request), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(created));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PatchMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UserUpdateRequest request,
                                   @AuthenticationPrincipal AppUserDetails actor) {
        return UserMapper.toResponse(userService.updateUser(id, request, actor));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails actor) {
        userService.deleteUser(id, actor);
        return ResponseEntity.noContent().build();
    }
}
