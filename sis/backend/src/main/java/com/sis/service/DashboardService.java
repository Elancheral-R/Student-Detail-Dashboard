package com.sis.service;

import com.sis.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Aggregates statistics for the admin dashboard.
 */
@Service
public class DashboardService {

    private final StudentRepository studentRepo;
    private final CourseRepository  courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final FeeRepository feeRepo;

    public DashboardService(StudentRepository studentRepo,
                            CourseRepository courseRepo,
                            EnrollmentRepository enrollmentRepo,
                            FeeRepository feeRepo) {
        this.studentRepo    = studentRepo;
        this.courseRepo     = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.feeRepo        = feeRepo;
    }

    public Map<String, Object> getStats() {
        BigDecimal pending = feeRepo.totalPendingAll();

        return Map.of(
            "totalStudents",     studentRepo.countByActiveTrue(),
            "totalCourses",      courseRepo.countByActiveTrue(),
            "totalEnrollments",  enrollmentRepo.countActiveEnrollments(),
            "totalFeePending",   pending == null ? BigDecimal.ZERO : pending,
            "year1Students",     studentRepo.countByYearOfStudyAndActiveTrue(1),
            "year2Students",     studentRepo.countByYearOfStudyAndActiveTrue(2),
            "year3Students",     studentRepo.countByYearOfStudyAndActiveTrue(3),
            "year4Students",     studentRepo.countByYearOfStudyAndActiveTrue(4)
        );
    }
}
