package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "ux_payments_payment_key", columnList = "payment_key", unique = true),
                @Index(name = "ix_payments_order_id", columnList = "order_id"),
                @Index(name = "ix_payments_created_at", columnList = "created_at"),
                @Index(name = "ux_payments_external_order_id", columnList = "external_order_id", unique = true)
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", nullable = false, length = 200, unique = true)
    private String paymentKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "external_order_id", nullable = false, length = 100)
    private String externalOrderId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "paid_total_after_payment", nullable = false)
    private Integer paidTotalAfterPayment;

    @Lob
    @Column(name = "raw", columnDefinition = "TEXT", nullable = false)
    private String raw; // TossConfirm 성공 응답 전체

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void pre() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public static Payment from(Order order,
                               String paymentKey,
                               String externalOrderId,
                               Integer amount,
                               Integer paidTotalAfterPayment,
                               String rawJson) {
        return Payment.builder()
                .paymentKey(paymentKey)
                .order(order)
                .externalOrderId(externalOrderId)
                .amount(amount)
                .paidTotalAfterPayment(paidTotalAfterPayment)
                .raw(rawJson)
                .build();
    }
}
