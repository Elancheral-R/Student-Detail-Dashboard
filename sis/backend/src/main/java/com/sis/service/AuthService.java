package com.sis.service;

import com.sis.config.JwtUtils;
import com.sis.exception.GlobalExceptionHandler.*;
import com.sis.model.User;
import com.sis.repository.UserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user authentication (login) and registration.
 * Returns a JWT token upon successful login.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Authenticate user and return JWT token.
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse login(String username, String password) {
        // Delegate to Spring Security — throws if invalid
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDetails ud = userDetailsService.loadUserByUsername(username);
        String token = jwtUtils.generateToken(ud, user.getRole().name());

        return new AuthResponse(token, user.getRole().name(), user.getFullName(), user.getId());
    }

    /** Register a new user (Admin only in production). */
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new DuplicateResourceException("Username already taken: " + user.getUsername());
        if (userRepository.existsByEmail(user.getEmail()))
            throw new DuplicateResourceException("Email already registered: " + user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        return userRepository.save(user);
    }

    // ── Response DTOs ──────────────────────────────────────────────────────────

    public record AuthResponse(String token, String role, String fullName, Long userId) {}
}
