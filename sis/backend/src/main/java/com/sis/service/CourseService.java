package com.sis.service;

import com.sis.exception.GlobalExceptionHandler.*;
import com.sis.model.*;
import com.sis.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Business logic for Course management, including enrollment.
 */
@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final StudentRepository studentRepo;
    private final FacultyRepository facultyRepo;

    public CourseService(CourseRepository courseRepo, EnrollmentRepository enrollmentRepo,
                         StudentRepository studentRepo, FacultyRepository facultyRepo) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.studentRepo = studentRepo;
        this.facultyRepo = facultyRepo;
    }

    public List<Course> getAllActive() { return courseRepo.findByActiveTrue(); }

    public Course getById(Long id) {
        return courseRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    public Course create(Course course) {
        if (courseRepo.existsByCourseCode(course.getCourseCode()))
            throw new DuplicateResourceException("Course code already exists: " + course.getCourseCode());
        return courseRepo.save(course);
    }

    public Course update(Long id, Course updated) {
        Course existing = getById(id);
        existing.setCourseName(updated.getCourseName());
        existing.setCredits(updated.getCredits());
        existing.setSemester(updated.getSemester());
        existing.setDescription(updated.getDescription());
        existing.setMaxStudents(updated.getMaxStudents());
        if (updated.getFaculty() != null)
            existing.setFaculty(facultyRepo.findById(updated.getFaculty().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found")));
        return courseRepo.save(existing);
    }

    public void delete(Long id) {
        Course c = getById(id);
        c.setActive(false);
        courseRepo.save(c);
    }

    /** Enroll a student in a course */
    public Enrollment enroll(Long studentId, Long courseId) {
        if (enrollmentRepo.existsByStudentIdAndCourseId(studentId, courseId))
            throw new DuplicateResourceException("Student already enrolled in this course");

        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = getById(courseId);

        // Capacity check
        long enrolled = enrollmentRepo.findByCourseId(courseId).stream()
            .filter(e -> e.getStatus() == Enrollment.Status.ACTIVE).count();
        if (enrolled >= course.getMaxStudents())
            throw new BusinessException("Course is at full capacity");

        Enrollment e = Enrollment.builder()
            .student(student).course(course).status(Enrollment.Status.ACTIVE).build();
        return enrollmentRepo.save(e);
    }

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepo.findByStudentId(studentId);
    }

    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepo.findByCourseId(courseId);
    }

    public long getTotalCount() { return courseRepo.countByActiveTrue(); }
}
