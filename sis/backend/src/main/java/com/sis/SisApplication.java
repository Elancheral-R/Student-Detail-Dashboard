package com.sis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Student Information System Spring Boot application.
 * Enables JPA auditing for automatic timestamp management.
 */
@SpringBootApplication
@EnableJpaAuditing
public class SisApplication {
    public static void main(String[] args) {
        SpringApplication.run(SisApplication.class, args);
    }
}
