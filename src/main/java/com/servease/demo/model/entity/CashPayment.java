package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "cash_payments",
        indexes = {
                @Index(name = "ix_cash_payments_order_id", columnList = "order_id"),
                @Index(name = "ix_cash_payments_received_at", columnList = "received_at")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "paid_total_after_payment", nullable = false)
    private Integer paidTotalAfterPayment;

    @Column(name = "received_at", nullable = false, updatable = false)
    private OffsetDateTime receivedAt;

    @PrePersist
    void onCreate() {
        if (receivedAt == null) {
            receivedAt = OffsetDateTime.now();
        }
    }

    public static CashPayment of(Order order, Integer amount, Integer paidTotalAfterPayment) {
        return CashPayment.builder()
                .order(order)
                .amount(amount)
                .paidTotalAfterPayment(paidTotalAfterPayment)
                .build();
    }
}
