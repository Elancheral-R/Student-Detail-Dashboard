package com.sis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Course entity. A course belongs to one department, has one faculty,
 * and can have many enrolled students (via Enrollment join table).
 */
@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", unique = true, nullable = false, length = 20)
    @NotBlank
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 150)
    @NotBlank
    private String courseName;

    @Column(nullable = false)
    @Min(1) @Max(6)
    @Builder.Default
    private Integer credits = 3;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    /** Faculty assigned to teach this course */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Column(nullable = false)
    @Min(1) @Max(8)
    @Builder.Default
    private Integer semester = 1;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_students")
    @Builder.Default
    private Integer maxStudents = 60;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Enrollment> enrollments;
}
