package com.sis.controller;

import com.sis.model.Student;
import com.sis.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Student CRUD operations.
 * Base URL: /api/students
 */
@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** GET /api/students — list all active students */
    @GetMapping
    public ResponseEntity<List<Student>> getAll() {
        return ResponseEntity.ok(studentService.getAllActive());
    }

    /** GET /api/students/{id} — get student by DB id */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    /** GET /api/students/search?q=arjun&page=0&size=10 */
    @GetMapping("/search")
    public ResponseEntity<Page<Student>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(studentService.search(q, page, size));
    }

    /** GET /api/students/department/{deptId} */
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<Student>> getByDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(studentService.getByDepartment(deptId));
    }

    /** POST /api/students — create student */
    @PostMapping
    public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
        return ResponseEntity.status(201).body(studentService.create(student));
    }

    /** PUT /api/students/{id} — update student */
    @PutMapping("/{id}")
    public ResponseEntity<Student> update(@PathVariable Long id,
                                          @Valid @RequestBody Student student) {
        return ResponseEntity.ok(studentService.update(id, student));
    }

    /** DELETE /api/students/{id} — soft delete (Admin only) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
