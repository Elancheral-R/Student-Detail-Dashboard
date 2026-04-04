package com.sis.repository;
import com.sis.model.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarksRepository extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentId(Long studentId);
    List<Marks> findByCourseId(Long courseId);
    List<Marks> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Marks> findByStudentIdAndCourseIdAndExamType(Long studentId, Long courseId, Marks.ExamType examType);

    /** Average percentage for a student across all courses */
    @Query("SELECT AVG((m.obtained / m.maxMarks) * 100) FROM Marks m WHERE m.student.id = :sid")
    Double averagePercentage(@Param("sid") Long studentId);

    /** All marks for a course (for faculty view) */
    @Query("SELECT m FROM Marks m WHERE m.course.id = :cid ORDER BY m.student.studentId")
    List<Marks> findByCourseIdOrdered(@Param("cid") Long courseId);
}
