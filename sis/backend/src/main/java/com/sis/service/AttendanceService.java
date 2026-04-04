package com.sis.service;

import com.sis.exception.GlobalExceptionHandler.*;
import com.sis.model.*;
import com.sis.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business logic for Attendance management.
 * Calculates attendance percentage and handles bulk marking.
 */
@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;

    public AttendanceService(AttendanceRepository attendanceRepo,
                             StudentRepository studentRepo,
                             CourseRepository courseRepo) {
        this.attendanceRepo = attendanceRepo;
        this.studentRepo    = studentRepo;
        this.courseRepo     = courseRepo;
    }

    public List<Attendance> getByStudent(Long studentId) {
        return attendanceRepo.findByStudentId(studentId);
    }

    public List<Attendance> getByStudentAndCourse(Long studentId, Long courseId) {
        return attendanceRepo.findByStudentIdAndCourseId(studentId, courseId);
    }

    public List<Attendance> getByDateAndCourse(LocalDate date, Long courseId) {
        return attendanceRepo.findByClassDateAndCourseId(date, courseId);
    }

    /**
     * Mark or update attendance for a single student on a given date.
     * Uses upsert semantics: creates a new record or updates an existing one.
     */
    public Attendance markAttendance(Long studentId, Long courseId,
                                     LocalDate date, Attendance.AttendanceStatus status,
                                     String remarks) {
        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepo.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Upsert: find existing or create new
        Attendance att = attendanceRepo
            .findByStudentIdAndCourseIdAndClassDate(studentId, courseId, date)
            .orElse(Attendance.builder()
                .student(student).course(course).classDate(date).build());

        att.setStatus(status);
        att.setRemarks(remarks);
        return attendanceRepo.save(att);
    }

    /**
     * Calculate attendance percentage for a student in a specific course.
     * Returns a map with present, total, and percentage.
     */
    public Map<String, Object> getAttendancePercentage(Long studentId, Long courseId) {
        long present = attendanceRepo.countPresent(studentId, courseId);
        long total   = attendanceRepo.countTotal(studentId, courseId);
        double pct   = total == 0 ? 0.0 : (present * 100.0) / total;

        return Map.of(
            "studentId", studentId,
            "courseId",  courseId,
            "present",   present,
            "total",     total,
            "percentage", Math.round(pct * 100.0) / 100.0
        );
    }

    public Attendance update(Long id, Attendance.AttendanceStatus status, String remarks) {
        Attendance att = attendanceRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        att.setStatus(status);
        att.setRemarks(remarks);
        return attendanceRepo.save(att);
    }
}
