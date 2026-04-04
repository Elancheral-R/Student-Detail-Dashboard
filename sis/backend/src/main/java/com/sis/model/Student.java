package com.sis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Student entity — core model of the SIS.
 * Has One-to-Many relationships with Attendance, Marks, Fees, and Enrollments.
 */
@Entity
@Table(name = "students")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", unique = true, nullable = false, length = 20)
    private String studentId;

    /** One-to-One link to users table for login */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    @Email
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "dob")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    /** Many students belong to one department */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "year_of_study", nullable = false)
    @Min(1) @Max(6)
    private Integer yearOfStudy;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** One student → many enrollments */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Enrollment> enrollments;

    /** One student → many attendance records */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Attendance> attendanceList;

    /** One student → many marks */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Marks> marksList;

    /** Convenience method: full name */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum Gender { MALE, FEMALE, OTHER }
}
