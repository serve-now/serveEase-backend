package com.servease.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_orders_order_id", columnNames = "order_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //외부 결제 연동에 사용되는 orderId: 토스에 보내주는거임
    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private RestaurantTable restaurantTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private Integer totalPrice = 0;

    @Column(name = "paid_amount", nullable = false)
    @Builder.Default
    private Integer paidAmount = 0;

    @Column(name = "remaining_amount", nullable = false)
    @Builder.Default
    private Integer remainingAmount = 0;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid = false;

    @CreationTimestamp
    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        this.orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    public void calculateTotalPrice() {
        this.totalPrice = this.orderItems.stream()
                .mapToInt(item -> item.getQuantity() * item.getItemPrice())
                .sum();
        syncPaymentAmountsWithTotal();
    }

    public void removeItemById(Long orderItemId){
        if (this.status == OrderStatus.COMPLETED
                || this.status == OrderStatus.CANCELED
                || this.status == OrderStatus.REFUNDED
                || this.status == OrderStatus.PARTIALLY_REFUNDED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "Cannot remove items from a completed or canceled order.");
        }

        OrderItem itemToRemove = this.orderItems.stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND, "OrderItem with ID " + orderItemId + " not found in this order."));

        this.removeOrderItem(itemToRemove);
        this.calculateTotalPrice();
    }

    //PG 승인 이후 금액을 주문에 반영하고 그에 따라 상태를 갱신
    public boolean recordPayment(Integer paymentAmount) {
        if (paymentAmount == null || paymentAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "결제 금액은 0보다 커야 합니다.");
        }
        if (this.isPaid) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID, "이미 결제가 완료된 주문입니다.");
        }
        if (this.status == OrderStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "환불된 주문에는 결제를 진행할 수 없습니다.");
        }
        int newPaidAmount = this.paidAmount + paymentAmount;
        if (newPaidAmount > this.totalPrice) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_EXCEEDS_REMAINING,
                    "결제 금액이 남은 금액을 초과했습니다.");
        }

        this.paidAmount = newPaidAmount;
        this.remainingAmount = this.totalPrice - this.paidAmount;

        if (this.remainingAmount == 0) {
            this.isPaid = true;
            this.status = OrderStatus.COMPLETED;
            if (this.paidAt == null) {
                this.paidAt = OffsetDateTime.now(ZoneOffset.UTC);
            }
            return true;
        }

        this.isPaid = false;
        if (this.status != OrderStatus.CANCELED) {
            if (this.status != OrderStatus.PARTIALLY_REFUNDED) {
                this.status = OrderStatus.PARTIALLY_PAID;
            }
        }
        return false;
    }

    public void refundPayment(Integer refundAmount) {
        if (refundAmount == null || refundAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "환불 금액은 0보다 커야 합니다.");
        }
        if (this.paidAmount == null || refundAmount > this.paidAmount) {
            throw new BusinessException(ErrorCode.CANCEL_AMOUNT_EXCEEDS_PAID, "환불 금액이 결제 금액을 초과했습니다.");
        }

        boolean wasFullyPaid = this.isPaid;
        boolean wasPartiallyRefunded = this.status == OrderStatus.PARTIALLY_REFUNDED;

        int updatedPaidAmount = this.paidAmount - refundAmount;
        if (updatedPaidAmount < 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "환불 처리 중 결제 금액 계산이 잘못되었습니다.");
        }

        this.paidAmount = updatedPaidAmount;
        this.remainingAmount = Math.max(0, this.totalPrice - this.paidAmount);

        if (this.paidAmount == 0) {
            this.isPaid = false;
            this.paidAt = null;
            if (this.status != OrderStatus.CANCELED) {
                this.status = OrderStatus.REFUNDED;
            }
            return;
        }

        if (this.paidAmount < this.totalPrice) {
            this.isPaid = false;
            this.paidAt = null;
            if (this.status != OrderStatus.CANCELED) {
                if (wasFullyPaid || wasPartiallyRefunded) {
                    this.status = OrderStatus.PARTIALLY_REFUNDED;
                } else {
                    this.status = OrderStatus.PARTIALLY_PAID;
                }
            }
            return;
        }

        this.isPaid = true;
        if (this.status != OrderStatus.CANCELED) {
            this.status = OrderStatus.COMPLETED;
        }
    }

    public void syncPaymentAmountsWithTotal() {
        if (this.paidAmount == null) {
            this.paidAmount = 0;
        }
        if (this.paidAmount > this.totalPrice) {
            throw new BusinessException(ErrorCode.PAID_AMOUNT_EXCEEDS_TOTAL,
                    "누적 결제 금액이 총 주문 금액을 초과했습니다.");
        }
        this.remainingAmount = this.totalPrice - this.paidAmount;
        if (this.remainingAmount == 0 && this.totalPrice > 0) {
            this.isPaid = true;
            if (this.status != OrderStatus.CANCELED) {
                this.status = OrderStatus.COMPLETED;
            }
            if (this.paidAt == null) {
                this.paidAt = OffsetDateTime.now(ZoneOffset.UTC);
            }
        } else {
            this.isPaid = false;
            if (this.paidAmount > 0 && this.status != OrderStatus.CANCELED) {
                if (this.status != OrderStatus.PARTIALLY_REFUNDED) {
                    this.status = OrderStatus.PARTIALLY_PAID;
                }
            }
            this.paidAt = null;
        }
    }

    public void revertFullPayment() {
        this.paidAmount = 0;
        this.remainingAmount = this.totalPrice;
        this.isPaid = false;
        this.paidAt = null;

        if (this.status != OrderStatus.CANCELED) {
            this.status = OrderStatus.REFUNDED;
        }
    }
}
