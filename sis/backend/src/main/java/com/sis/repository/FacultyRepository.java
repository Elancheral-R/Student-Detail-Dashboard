package com.sis.repository;
import com.sis.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByFacultyId(String facultyId);
    Optional<Faculty> findByEmail(String email);
    Optional<Faculty> findByUserId(Long userId);
    List<Faculty> findByActiveTrue();
    List<Faculty> findByDepartmentIdAndActiveTrue(Long departmentId);
}
