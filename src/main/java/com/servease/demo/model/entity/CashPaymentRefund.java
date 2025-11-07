package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "cash_payment_refunds",
        indexes = {
                @Index(name = "ix_cash_payment_refunds_cash_payment_id", columnList = "cash_payment_id"),
                @Index(name = "ix_cash_payment_refunds_refunded_at", columnList = "refunded_at")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashPaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cash_payment_id", nullable = false)
    private CashPayment cashPayment;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (refundedAt == null) {
            refundedAt = now;
        }
    }

    public static CashPaymentRefund of(CashPayment cashPayment,
                                       Integer refundAmount,
                                       String refundReason,
                                       OffsetDateTime refundedAt) {
        return CashPaymentRefund.builder()
                .cashPayment(cashPayment)
                .refundAmount(refundAmount)
                .refundReason(refundReason)
                .refundedAt(refundedAt)
                .build();
    }
}
