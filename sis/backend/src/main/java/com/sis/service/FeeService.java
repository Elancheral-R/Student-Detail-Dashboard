package com.sis.service;

import com.sis.exception.GlobalExceptionHandler.*;
import com.sis.model.*;
import com.sis.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Handles fee payments, receipt generation, and pending fee queries.
 */
@Service
@Transactional
public class FeeService {

    private final FeeRepository feeRepo;
    private final StudentRepository studentRepo;

    public FeeService(FeeRepository feeRepo, StudentRepository studentRepo) {
        this.feeRepo    = feeRepo;
        this.studentRepo = studentRepo;
    }

    public List<Fee> getByStudent(Long studentId) {
        return feeRepo.findByStudentId(studentId);
    }

    public List<Fee> getPending() {
        return feeRepo.findByStatus(Fee.FeeStatus.PENDING);
    }

    public Fee createFeeRecord(Long studentId, Fee.FeeType feeType,
                               BigDecimal amount, LocalDate dueDate,
                               String academicYear) {
        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Fee fee = Fee.builder()
            .student(student)
            .feeType(feeType)
            .amount(amount)
            .amountPaid(BigDecimal.ZERO)
            .dueDate(dueDate)
            .academicYear(academicYear)
            .status(Fee.FeeStatus.PENDING)
            .build();
        return feeRepo.save(fee);
    }

    /**
     * Record a payment towards a fee.
     * Automatically updates status to PARTIAL or PAID.
     */
    public Fee recordPayment(Long feeId, BigDecimal paymentAmount, Fee.PaymentMethod method) {
        Fee fee = feeRepo.findById(feeId)
            .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));

        if (fee.getStatus() == Fee.FeeStatus.PAID)
            throw new BusinessException("This fee has already been fully paid");

        BigDecimal newPaid = fee.getAmountPaid().add(paymentAmount);
        if (newPaid.compareTo(fee.getAmount()) > 0)
            throw new BusinessException("Payment exceeds due amount");

        fee.setAmountPaid(newPaid);
        fee.setPaidDate(LocalDate.now());
        fee.setPaymentMethod(method);

        if (newPaid.compareTo(fee.getAmount()) == 0) {
            fee.setStatus(Fee.FeeStatus.PAID);
            fee.setReceiptNo("REC" + System.currentTimeMillis());
        } else {
            fee.setStatus(Fee.FeeStatus.PARTIAL);
        }

        return feeRepo.save(fee);
    }

    public BigDecimal getTotalPendingForStudent(Long studentId) {
        BigDecimal total = feeRepo.totalPendingForStudent(studentId);
        return total == null ? BigDecimal.ZERO : total;
    }
}
