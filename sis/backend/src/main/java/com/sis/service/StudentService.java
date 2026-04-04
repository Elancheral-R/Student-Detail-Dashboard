package com.sis.service;

import com.sis.exception.GlobalExceptionHandler.*;
import com.sis.model.Department;
import com.sis.model.Student;
import com.sis.repository.DepartmentRepository;
import com.sis.repository.StudentRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Business logic for Student CRUD operations.
 * Encapsulates validation, studentId generation, and search.
 */
@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepo;
    private final DepartmentRepository deptRepo;

    public StudentService(StudentRepository studentRepo, DepartmentRepository deptRepo) {
        this.studentRepo = studentRepo;
        this.deptRepo = deptRepo;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Student> getAllActive() {
        return studentRepo.findByActiveTrue();
    }

    public Student getById(Long id) {
        return studentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public Student getByStudentId(String studentId) {
        return studentRepo.findByStudentId(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    }

    public Page<Student> search(String query, int page, int size) {
        return studentRepo.search(query, PageRequest.of(page, size));
    }

    public List<Student> getByDepartment(Long deptId) {
        return studentRepo.findByDepartmentIdAndActiveTrue(deptId);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public Student create(Student student) {
        // Email uniqueness check
        if (studentRepo.findByEmail(student.getEmail()).isPresent())
            throw new DuplicateResourceException("Email already registered: " + student.getEmail());

        // Validate department
        if (student.getDepartment() != null && student.getDepartment().getId() != null) {
            Department dept = deptRepo.findById(student.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            student.setDepartment(dept);
        }

        // Auto-generate studentId: STU + YEAR + sequence
        student.setStudentId(generateStudentId());

        if (student.getAdmissionDate() == null)
            student.setAdmissionDate(LocalDate.now());

        return studentRepo.save(student);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public Student update(Long id, Student updated) {
        Student existing = getById(id);

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setDob(updated.getDob());
        existing.setGender(updated.getGender());
        existing.setAddress(updated.getAddress());
        existing.setYearOfStudy(updated.getYearOfStudy());

        // Update department if changed
        if (updated.getDepartment() != null && updated.getDepartment().getId() != null) {
            Department dept = deptRepo.findById(updated.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            existing.setDepartment(dept);
        }

        return studentRepo.save(existing);
    }

    // ── DELETE (soft-delete) ──────────────────────────────────────────────────

    public void delete(Long id) {
        Student student = getById(id);
        student.setActive(false);    // Soft delete preserves historical records
        studentRepo.save(student);
    }

    // ── STATS for Dashboard ───────────────────────────────────────────────────

    public long getTotalCount()     { return studentRepo.countByActiveTrue(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateStudentId() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count  = studentRepo.count() + 1;
        return "STU" + year + String.format("%03d", count);
    }
}
