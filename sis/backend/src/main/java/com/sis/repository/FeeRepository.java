package com.sis.repository;
import com.sis.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentId(Long studentId);
    List<Fee> findByStatus(Fee.FeeStatus status);
    List<Fee> findByStudentIdAndAcademicYear(Long studentId, String academicYear);

    @Query("SELECT SUM(f.amount - f.amountPaid) FROM Fee f WHERE f.student.id = :sid AND f.status != 'PAID'")
    BigDecimal totalPendingForStudent(@Param("sid") Long studentId);

    @Query("SELECT SUM(f.amount - f.amountPaid) FROM Fee f WHERE f.status != 'PAID'")
    BigDecimal totalPendingAll();
}
