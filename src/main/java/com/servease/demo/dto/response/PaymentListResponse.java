package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.entity.Payment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record PaymentListResponse(
        Long paymentId,
        String orderId,
        String paymentMethod,
        OffsetDateTime approvedAt,
        Integer totalPaymentAmount,
        String paymentStatus,
        String representativeItemName,
        Integer totalItemCount
) {
    public static PaymentListResponse from(Payment payment) {
        Objects.requireNonNull(payment, "payment가 null 입니다.");

        Order order = Objects.requireNonNull(payment.getOrder(), "order가 null 입니다.");
        List<OrderItem> orderItems = order.getOrderItems() != null ? order.getOrderItems() : List.of();

        String representativeItemName = null;
        if (!orderItems.isEmpty() && orderItems.get(0) != null && orderItems.get(0).getMenu() != null) {
            representativeItemName = orderItems.get(0).getMenu().getName();
        }

        int totalItemCount = orderItems.stream()
                .mapToInt(orderItem -> orderItem != null ? orderItem.getQuantity() : 0)
                .sum();

        return new PaymentListResponse(
                payment.getId(),
                order.getOrderId(),
                payment.getMethod(),
                payment.getApprovedAt(),
                payment.getAmount(),
                order.getStatus() != null ? order.getStatus().name() : null,
                representativeItemName,
                totalItemCount
        );
    }
}
