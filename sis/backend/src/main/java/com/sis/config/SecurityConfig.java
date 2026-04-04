package com.sis.config;

import com.sis.model.User;
import com.sis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

/**
 * Central Spring Security configuration.
 * - Stateless JWT sessions
 * - Role-based endpoint access
 * - CORS enabled for frontend origins
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${sis.cors.allowed-origins}")
    private String allowedOrigins;

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Public ──────────────────────────────────────────────────
                .requestMatchers("/auth/**").permitAll()

                // ── Admin-only ───────────────────────────────────────────────
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/courses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/fees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/timetable/**").hasRole("ADMIN")

                // ── Faculty + Admin (write attendance & marks) ───────────────
                .requestMatchers(HttpMethod.POST,   "/attendance/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.PUT,    "/attendance/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.PATCH,  "/attendance/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.POST,   "/marks/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.PUT,    "/marks/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.PATCH,  "/marks/**").hasAnyRole("ADMIN","FACULTY")

                // ── Students write-block (only admin/faculty may create/update) ──
                .requestMatchers(HttpMethod.POST,   "/students/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.PUT,    "/students/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.PATCH,  "/students/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.POST,   "/courses/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.PUT,    "/courses/**").hasAnyRole("ADMIN","FACULTY")
                .requestMatchers(HttpMethod.POST,   "/fees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/fees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,   "/timetable/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/timetable/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/timetable/**").hasRole("ADMIN")

                // ── All authenticated (read) ─────────────────────────────────
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Load users from the database for authentication */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getActive(),   // enabled
                true, true, true,   // accountNonExpired, credentialsNonExpired, accountNonLocked
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
