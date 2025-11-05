package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.entity.Payment;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record OrderPaymentListResponse(
        String orderId,
        Integer totalPaymentAmount,
        Integer splitCount,
        String representativePaymentMethod,
        String representativePaymentStatus,
        OffsetDateTime representativeApprovedAt,
        String representativeItemName,
        Integer totalItemCount
) {

    public static OrderPaymentListResponse from(Order order, List<Payment> payments) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(payments, "payments must not be null");

        if (payments.isEmpty()) {
            throw new IllegalArgumentException("payments must not be empty");
        }

        List<Payment> sortedPayments = payments.stream()
                .sorted(Comparator.comparing(OrderPaymentListResponse::sortKey).reversed())
                .toList();

        int totalAmount = sortedPayments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        Payment representative = sortedPayments.get(0);

        String representativeItemName = order.getOrderItems() != null && !order.getOrderItems().isEmpty()
                ? extractItemName(order.getOrderItems().get(0))
                : null;

        int totalItemCount = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .filter(Objects::nonNull)
                .mapToInt(OrderItem::getQuantity)
                .sum()
                : 0;

        return new OrderPaymentListResponse(
                order.getOrderId(),
                totalAmount,
                sortedPayments.size(),
                representative.getMethod(),
                order.getStatus() != null ? order.getStatus().name() : null,
                representative.getApprovedAt(),
                representativeItemName,
                totalItemCount
        );
    }

    private static OffsetDateTime sortKey(Payment payment) {
        if (payment.getApprovedAt() != null) {
            return payment.getApprovedAt();
        }
        return payment.getCreatedAt();
    }

    private static String extractItemName(OrderItem orderItem) {
        if (orderItem == null || orderItem.getMenu() == null) {
            return null;
        }
        return orderItem.getMenu().getName();
    }
}
