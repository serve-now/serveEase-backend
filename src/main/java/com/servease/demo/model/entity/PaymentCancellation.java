package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_cancellations",
        indexes = {
                @Index(name = "ix_payment_cancellations_payment_id", columnList = "payment_id"),
                @Index(name = "ix_payment_cancellations_canceled_at", columnList = "canceled_at")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "cancel_amount", nullable = false)
    private Integer cancelAmount;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Lob
    @Column(name = "raw", columnDefinition = "TEXT", nullable = false)
    private String raw;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public static PaymentCancellation of(Payment payment,
                                         Integer cancelAmount,
                                         String cancelReason,
                                         OffsetDateTime canceledAt,
                                         String raw) {
        return PaymentCancellation.builder()
                .payment(payment)
                .cancelAmount(cancelAmount)
                .cancelReason(cancelReason)
                .canceledAt(canceledAt)
                .raw(raw)
                .build();
    }
}
