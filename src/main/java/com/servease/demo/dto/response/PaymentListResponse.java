package com.servease.demo.dto.response;

import com.servease.demo.model.entity.CashPayment;
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
        Objects.requireNonNull(payment, "payment must not be null");

        Order order = Objects.requireNonNull(payment.getOrder(), "order must not be null");
        List<OrderItem> orderItems = order.getOrderItems() != null ? order.getOrderItems() : List.of();

        return new PaymentListResponse(
                payment.getId(),
                order.getOrderId(),
                payment.getMethod(),
                payment.getApprovedAt(),
                payment.getAmount(),
                order.getStatus() != null ? order.getStatus().name() : null,
                resolveRepresentativeItemName(orderItems),
                calculateTotalItemCount(orderItems)
        );
    }

    public static PaymentListResponse fromCashPayment(CashPayment cashPayment) {
        Objects.requireNonNull(cashPayment, "cashPayment must not be null");

        Order order = Objects.requireNonNull(cashPayment.getOrder(), "order must not be null");
        List<OrderItem> orderItems = order.getOrderItems() != null ? order.getOrderItems() : List.of();

        return new PaymentListResponse(
                cashPayment.getId(),
                order.getOrderId(),
                "CASH",
                cashPayment.getReceivedAt(),
                cashPayment.getAmount(),
                order.getStatus() != null ? order.getStatus().name() : null,
                resolveRepresentativeItemName(orderItems),
                calculateTotalItemCount(orderItems)
        );
    }

    private static String resolveRepresentativeItemName(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            return null;
        }

        OrderItem firstItem = orderItems.get(0);
        if (firstItem == null || firstItem.getMenu() == null) {
            return null;
        }

        return firstItem.getMenu().getName();
    }

    private static int calculateTotalItemCount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToInt(orderItem -> orderItem != null ? orderItem.getQuantity() : 0)
                .sum();
    }
}
