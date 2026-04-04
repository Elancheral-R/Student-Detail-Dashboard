package com.sis.controller;

import com.sis.model.User;
import com.sis.service.AuthService;
import com.sis.repository.UserRepository;
import com.sis.exception.GlobalExceptionHandler.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only REST controller for managing system users.
 * All endpoints require ROLE_ADMIN.
 */
@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /** GET /api/users — list all users (password masked) */
    @GetMapping
    public ResponseEntity<List<UserResponse>> listAll() {
        List<UserResponse> users = userRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(UserResponse::from)
            .toList();
        return ResponseEntity.ok(users);
    }

    /** GET /api/users/role/{role} — list users by role */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> listByRole(@PathVariable String role) {
        User.Role r = User.Role.valueOf(role.toUpperCase());
        List<UserResponse> users = userRepository.findByRole(r)
            .stream()
            .map(UserResponse::from)
            .toList();
        return ResponseEntity.ok(users);
    }

    /** POST /api/users — create a new user (admin creates admin/faculty/student) */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        User user = User.builder()
            .username(req.username())
            .password(req.password())  // AuthService will encode it
            .email(req.email())
            .fullName(req.fullName())
            .role(User.Role.valueOf(req.role().toUpperCase()))
            .active(true)
            .build();

        User saved = authService.register(user);
        return ResponseEntity.status(201).body(UserResponse.from(saved));
    }

    /** PUT /api/users/{id}/toggle — activate / deactivate a user */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<UserResponse> toggleActive(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(!user.getActive());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(saved));
    }

    /** DELETE /api/users/{id} — delete a user */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException("User not found: " + id);
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    /** Request body for creating a new user */
    public record CreateUserRequest(
        @NotBlank(message = "Username required") String username,
        @NotBlank(message = "Password required") String password,
        @Email(message = "Valid email required") @NotBlank String email,
        @NotBlank(message = "Full name required") String fullName,
        @NotBlank(message = "Role required")     String role
    ) {}

    /** Safe response — never includes password hash */
    public record UserResponse(
        Long id, String username, String email, String fullName,
        String role, Boolean active, String createdAt
    ) {
        static UserResponse from(User u) {
            return new UserResponse(
                u.getId(), u.getUsername(), u.getEmail(), u.getFullName(),
                u.getRole().name(), u.getActive(),
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : null
            );
        }
    }
}
