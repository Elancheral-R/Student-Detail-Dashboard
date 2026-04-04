package com.sis.controller;

import com.sis.model.User;
import com.sis.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * All routes under /api/auth are publicly accessible (no JWT required).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Body: { "username": "admin", "password": "password123" }
     * Returns: { "token": "...", "role": "ADMIN", "fullName": "...", "userId": 1 }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthService.AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthService.AuthResponse response = authService.login(req.username(), req.password());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register  (Admin use only in production)
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        User saved = authService.register(user);
        saved.setPassword(null); // Never return password hash
        return ResponseEntity.status(201).body(saved);
    }

    public record LoginRequest(
        @NotBlank(message = "Username required") String username,
        @NotBlank(message = "Password required") String password
    ) {}
}
