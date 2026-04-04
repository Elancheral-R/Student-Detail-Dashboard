package com.sis.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Examination marks for a student in a specific course and exam type.
 * Grade is calculated in the service layer.
 */
@Entity
@Table(name = "marks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","course_id","exam_type"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;

    @Column(name = "max_marks", precision = 5, scale = 2)
    private BigDecimal maxMarks = BigDecimal.valueOf(100);

    @Column(name = "obtained", precision = 5, scale = 2)
    private BigDecimal obtained = BigDecimal.ZERO;

    @Column(length = 5)
    private String grade;

    @Column(length = 255)
    private String remarks;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by")
    private Faculty enteredBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum ExamType { INTERNAL1, INTERNAL2, MIDTERM, FINAL }
}
