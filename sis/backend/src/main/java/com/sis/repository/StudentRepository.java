package com.sis.repository;

import com.sis.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByEmail(String email);
    List<Student> findByActiveTrue();

    /** Search by name, email, or studentId (case-insensitive) */
    @Query("""
           SELECT s FROM Student s
           WHERE s.active = true AND (
               LOWER(s.firstName)  LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(s.lastName)   LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(s.email)      LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(s.studentId)  LIKE LOWER(CONCAT('%', :q, '%'))
           )""")
    Page<Student> search(@Param("q") String query, Pageable pageable);

    /** Filter by department */
    List<Student> findByDepartmentIdAndActiveTrue(Long departmentId);

    /** Count by year of study */
    long countByYearOfStudyAndActiveTrue(Integer year);

    /** Dashboard: total active students */
    long countByActiveTrue();
}
