package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.UserRequest;
import org.example.controller.dto.UserResponse;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return service.getAllUsers().stream().map(Mappers::toUserResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return service.getUserById(id)
                .map(Mappers::toUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest body) {
        var created = service.createUser(Mappers.toUser(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(Mappers.toUserResponse(created));
    }

    /** Частичное обновление: поля, не переданные в теле, остаются без изменений. */
    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody UserRequest body) {
        return Mappers.toUserResponse(service.updateUser(id, Mappers.toUser(body)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
