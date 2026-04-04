package com.sis.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fee record — tracks tuition, hostel, exam, and miscellaneous fee payments.
 */
@Entity
@Table(name = "fees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "receipt_no", length = 50)
    private String receiptNo;

    @Enumerated(EnumType.STRING)
    private FeeStatus status = FeeStatus.PENDING;

    @Column(length = 255)
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    /** Convenience: outstanding balance */
    public BigDecimal getBalance() {
        return amount.subtract(amountPaid);
    }

    public enum FeeType    { TUITION, HOSTEL, TRANSPORT, EXAM, MISC }
    public enum PaymentMethod { CASH, CARD, UPI, BANK_TRANSFER }
    public enum FeeStatus  { PENDING, PARTIAL, PAID }
}
