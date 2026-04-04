package com.sis.repository;
import com.sis.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByCourseId(Long courseId);
    List<Attendance> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Attendance> findByStudentIdAndCourseIdAndClassDate(Long studentId, Long courseId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :sid AND a.course.id = :cid AND a.status = 'PRESENT'")
    long countPresent(@Param("sid") Long studentId, @Param("cid") Long courseId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :sid AND a.course.id = :cid")
    long countTotal(@Param("sid") Long studentId, @Param("cid") Long courseId);

    List<Attendance> findByClassDateAndCourseId(LocalDate date, Long courseId);
}
