package com.sis.service;

import com.sis.exception.GlobalExceptionHandler.*;
import com.sis.model.*;
import com.sis.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Business logic for examination marks.
 * Computes grades (O/A+/A/B+/B/C/F) and GPA on a 10-point scale.
 */
@Service
@Transactional
public class MarksService {

    private final MarksRepository marksRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;

    public MarksService(MarksRepository marksRepo,
                        StudentRepository studentRepo,
                        CourseRepository courseRepo) {
        this.marksRepo  = marksRepo;
        this.studentRepo = studentRepo;
        this.courseRepo  = courseRepo;
    }

    public List<Marks> getByStudent(Long studentId) {
        return marksRepo.findByStudentId(studentId);
    }

    public List<Marks> getByStudentAndCourse(Long studentId, Long courseId) {
        return marksRepo.findByStudentIdAndCourseId(studentId, courseId);
    }

    public Marks enterMarks(Long studentId, Long courseId, Marks.ExamType examType,
                            BigDecimal maxMarks, BigDecimal obtained, String remarks) {
        if (obtained.compareTo(maxMarks) > 0)
            throw new BusinessException("Obtained marks cannot exceed max marks");

        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepo.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Marks marks = marksRepo
            .findByStudentIdAndCourseIdAndExamType(studentId, courseId, examType)
            .orElse(Marks.builder().student(student).course(course).examType(examType).build());

        marks.setMaxMarks(maxMarks);
        marks.setObtained(obtained);
        marks.setRemarks(remarks);
        marks.setGrade(calculateGrade(obtained, maxMarks));
        return marksRepo.save(marks);
    }

    /**
     * Generate a full report card for a student.
     * Returns subject-wise marks + grade + GPA.
     */
    public Map<String, Object> generateReportCard(Long studentId) {
        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        List<Marks> allMarks = marksRepo.findByStudentId(studentId);

        // Group by course
        Map<String, List<Marks>> byCourse = new LinkedHashMap<>();
        for (Marks m : allMarks) {
            byCourse.computeIfAbsent(m.getCourse().getCourseName(), k -> new ArrayList<>()).add(m);
        }

        double totalGradePoints = 0;
        int    totalCredits     = 0;

        List<Map<String, Object>> subjects = new ArrayList<>();
        for (var entry : byCourse.entrySet()) {
            List<Marks> mList = entry.getValue();
            double avg = mList.stream()
                .mapToDouble(m -> m.getObtained().doubleValue() / m.getMaxMarks().doubleValue() * 100)
                .average().orElse(0);

            int credits = mList.get(0).getCourse().getCredits();
            double gradePoint = percentageToGradePoint(avg);
            totalGradePoints += gradePoint * credits;
            totalCredits     += credits;

            Map<String, Object> sub = new LinkedHashMap<>();
            sub.put("course",       entry.getKey());
            sub.put("credits",      credits);
            sub.put("percentage",   Math.round(avg * 100.0) / 100.0);
            sub.put("grade",        calculateGradeFromPct(avg));
            sub.put("gradePoint",   gradePoint);
            subjects.add(sub);
        }

        double cgpa = totalCredits == 0 ? 0 : totalGradePoints / totalCredits;

        return Map.of(
            "studentId",  student.getStudentId(),
            "studentName", student.getFullName(),
            "department", student.getDepartment() != null ? student.getDepartment().getName() : "-",
            "subjects",   subjects,
            "cgpa",       Math.round(cgpa * 100.0) / 100.0,
            "totalCredits", totalCredits
        );
    }

    // ── Grade helpers ──────────────────────────────────────────────────────────

    /** Calculate grade from obtained/max */
    private String calculateGrade(BigDecimal obtained, BigDecimal max) {
        double pct = obtained.divide(max, 4, RoundingMode.HALF_UP).doubleValue() * 100;
        return calculateGradeFromPct(pct);
    }

    private String calculateGradeFromPct(double pct) {
        if (pct >= 90) return "O";
        if (pct >= 85) return "A+";
        if (pct >= 75) return "A";
        if (pct >= 65) return "B+";
        if (pct >= 55) return "B";
        if (pct >= 50) return "C";
        return "F";
    }

    /** Convert percentage to 10-point grade scale (Anna University style) */
    private double percentageToGradePoint(double pct) {
        if (pct >= 90) return 10.0;
        if (pct >= 85) return 9.0;
        if (pct >= 75) return 8.0;
        if (pct >= 65) return 7.0;
        if (pct >= 55) return 6.0;
        if (pct >= 50) return 5.0;
        return 0.0;
    }
}
