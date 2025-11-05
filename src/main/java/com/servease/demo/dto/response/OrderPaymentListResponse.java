package com.servease.demo.dto.response;

import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.entity.Payment;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    public static OrderPaymentListResponse from(Order order, List<Payment> payments, List<CashPayment> cashPayments) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(payments, "payments must not be null");
        Objects.requireNonNull(cashPayments, "cashPayments must not be null");

        if (payments.isEmpty() && cashPayments.isEmpty()) {
            throw new IllegalArgumentException("payments or cashPayments must not be empty");
        }

        List<Entry> entries = new ArrayList<>(payments.size() + cashPayments.size());

        for (Payment payment : payments) {
            entries.add(Entry.from(payment));
        }

        for (CashPayment cashPayment : cashPayments) {
            entries.add(Entry.from(cashPayment));
        }

        entries.sort(Comparator.comparing(Entry::sortKey, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        int totalAmount = entries.stream()
                .map(Entry::amount)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        Entry representative = entries.get(0);

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
                entries.size(),
                representative.method(),
                order.getStatus() != null ? order.getStatus().name() : null,
                representative.approvedAt(),
                representativeItemName,
                totalItemCount
        );
    }

    private static String extractItemName(OrderItem orderItem) {
        if (orderItem == null || orderItem.getMenu() == null) {
            return null;
        }
        return orderItem.getMenu().getName();
    }

    private record Entry(
            OffsetDateTime sortKey,
            Integer amount,
            String method,
            OffsetDateTime approvedAt
    ) {

        private static Entry from(Payment payment) {
            Objects.requireNonNull(payment, "payment must not be null");

            OffsetDateTime sortKey = payment.getApprovedAt();
            if (sortKey == null) {
                sortKey = payment.getCreatedAt();
            }

            return new Entry(
                    sortKey,
                    payment.getAmount(),
                    payment.getMethod(),
                    payment.getApprovedAt()
            );
        }

        private static Entry from(CashPayment cashPayment) {
            Objects.requireNonNull(cashPayment, "cashPayment must not be null");

            OffsetDateTime receivedAt = cashPayment.getReceivedAt();

            return new Entry(
                    receivedAt,
                    cashPayment.getAmount(),
                    "CASH",
                    receivedAt
            );
        }
    }
}
