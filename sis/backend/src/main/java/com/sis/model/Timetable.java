package com.sis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * Timetable — class schedule entry.
 * Unique per (course, day, startTime) to prevent double-booking.
 */
@Entity
@Table(name = "timetable",
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id","day_of_week","start_time"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "room_no", length = 20)
    private String roomNo;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "academic_year", length = 10)
    private String academicYear;

    public enum DayOfWeek { MON, TUE, WED, THU, FRI, SAT }
}
