package com.sis.controller;

import com.sis.model.Faculty;
import com.sis.repository.FacultyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Faculty-related operations.
 */
@RestController
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyRepository facultyRepository;

    public FacultyController(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    /**
     * GET /api/faculty — list all active faculty members.
     * Accessible by ADMIN and FACULTY.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<List<Faculty>> getAllActive() {
        return ResponseEntity.ok(facultyRepository.findByActiveTrue());
    }
}
