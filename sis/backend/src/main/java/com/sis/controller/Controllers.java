package com.sis.controller;

import com.sis.model.*;
import com.sis.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// ─── Course Controller ────────────────────────────────────────────────────────
@RestController
@RequestMapping("/courses")
class CourseController {
    private final CourseService courseService;

    CourseController(CourseService courseService) { this.courseService = courseService; }

    @GetMapping
    public ResponseEntity<List<Course>> getAll() { return ResponseEntity.ok(courseService.getAllActive()); }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) { return ResponseEntity.ok(courseService.getById(id)); }

    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody Course course) {
        return ResponseEntity.status(201).body(courseService.create(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course course) {
        return ResponseEntity.ok(courseService.update(id, course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id); return ResponseEntity.noContent().build();
    }

    /** POST /api/courses/{courseId}/enroll/{studentId} */
    @PostMapping("/{courseId}/enroll/{studentId}")
    public ResponseEntity<Enrollment> enroll(@PathVariable Long courseId, @PathVariable Long studentId) {
        return ResponseEntity.status(201).body(courseService.enroll(studentId, courseId));
    }

    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<List<Enrollment>> getCourseEnrollments(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getEnrollmentsByCourse(courseId));
    }

    @GetMapping("/student/{studentId}/enrollments")
    public ResponseEntity<List<Enrollment>> getStudentEnrollments(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.getEnrollmentsByStudent(studentId));
    }
}

// ─── Attendance Controller ─────────────────────────────────────────────────────
@RestController
@RequestMapping("/attendance")
class AttendanceController {
    private final AttendanceService attendanceService;

    AttendanceController(AttendanceService attendanceService) { this.attendanceService = attendanceService; }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Attendance>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<List<Attendance>> getByStudentAndCourse(
            @PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(attendanceService.getByStudentAndCourse(studentId, courseId));
    }

    /** GET /api/attendance/student/{sid}/course/{cid}/percentage */
    @GetMapping("/student/{studentId}/course/{courseId}/percentage")
    public ResponseEntity<Map<String, Object>> getPercentage(
            @PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(attendanceService.getAttendancePercentage(studentId, courseId));
    }

    /** POST /api/attendance/mark */
    @PostMapping("/mark")
    public ResponseEntity<Attendance> mark(@RequestBody AttendanceRequest req) {
        return ResponseEntity.ok(attendanceService.markAttendance(
            req.studentId(), req.courseId(), req.classDate(), req.status(), req.remarks()));
    }

    @GetMapping("/course/{courseId}/date/{date}")
    public ResponseEntity<List<Attendance>> getByDateAndCourse(
            @PathVariable Long courseId, @PathVariable String date) {
        return ResponseEntity.ok(attendanceService.getByDateAndCourse(LocalDate.parse(date), courseId));
    }

    record AttendanceRequest(Long studentId, Long courseId, LocalDate classDate,
                              Attendance.AttendanceStatus status, String remarks) {}
}

// ─── Marks Controller ──────────────────────────────────────────────────────────
@RestController
@RequestMapping("/marks")
class MarksController {
    private final MarksService marksService;

    MarksController(MarksService marksService) { this.marksService = marksService; }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Marks>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(marksService.getByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<List<Marks>> getByStudentAndCourse(
            @PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(marksService.getByStudentAndCourse(studentId, courseId));
    }

    /** GET /api/marks/student/{id}/reportcard */
    @GetMapping("/student/{studentId}/reportcard")
    public ResponseEntity<Map<String, Object>> getReportCard(@PathVariable Long studentId) {
        return ResponseEntity.ok(marksService.generateReportCard(studentId));
    }

    @PostMapping("/enter")
    public ResponseEntity<Marks> enterMarks(@RequestBody MarksRequest req) {
        return ResponseEntity.ok(marksService.enterMarks(
            req.studentId(), req.courseId(), req.examType(),
            req.maxMarks(), req.obtained(), req.remarks()));
    }

    record MarksRequest(Long studentId, Long courseId, Marks.ExamType examType,
                        BigDecimal maxMarks, BigDecimal obtained, String remarks) {}
}

// ─── Fee Controller ────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/fees")
class FeeController {
    private final FeeService feeService;

    FeeController(FeeService feeService) { this.feeService = feeService; }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Fee>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(feeService.getByStudent(studentId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Fee>> getPending() {
        return ResponseEntity.ok(feeService.getPending());
    }

    @GetMapping("/student/{studentId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long studentId) {
        return ResponseEntity.ok(Map.of(
            "studentId", studentId,
            "totalPending", feeService.getTotalPendingForStudent(studentId)));
    }

    @PostMapping
    public ResponseEntity<Fee> create(@RequestBody FeeCreateRequest req) {
        return ResponseEntity.status(201).body(feeService.createFeeRecord(
            req.studentId(), req.feeType(), req.amount(), req.dueDate(), req.academicYear()));
    }

    @PostMapping("/{feeId}/pay")
    public ResponseEntity<Fee> pay(@PathVariable Long feeId, @RequestBody PaymentRequest req) {
        return ResponseEntity.ok(feeService.recordPayment(feeId, req.amount(), req.method()));
    }

    record FeeCreateRequest(Long studentId, Fee.FeeType feeType, BigDecimal amount,
                             LocalDate dueDate, String academicYear) {}
    record PaymentRequest(BigDecimal amount, Fee.PaymentMethod method) {}
}

// ─── Dashboard Controller ──────────────────────────────────────────────────────
@RestController
@RequestMapping("/dashboard")
class DashboardController {
    private final DashboardService dashboardService;

    DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
