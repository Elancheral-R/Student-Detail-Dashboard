package com.sis.repository;
import com.sis.model.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findBySemesterAndAcademicYear(Integer semester, String academicYear);
    List<Timetable> findByCourseId(Long courseId);

    /** Conflict check: same room, same day, overlapping times */
    @Query("""
        SELECT t FROM Timetable t
        WHERE t.roomNo = :room
          AND t.dayOfWeek = :day
          AND t.startTime < :end
          AND t.endTime   > :start
          AND (:id IS NULL OR t.id != :id)
        """)
    List<Timetable> findConflicts(
            @Param("room") String room,
            @Param("day")  Timetable.DayOfWeek day,
            @Param("start") LocalTime start,
            @Param("end")   LocalTime end,
            @Param("id")    Long excludeId);
}
