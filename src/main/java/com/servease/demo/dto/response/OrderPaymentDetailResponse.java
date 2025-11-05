package com.servease.demo.dto.response;

import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record OrderPaymentDetailResponse(
        String orderId,
        Integer totalPaymentAmount,
        Integer splitCount,
        String representativePaymentMethod,
        String representativePaymentStatus,
        ZonedDateTime representativeApprovedAt,
        Integer tableNumber,
        String orderStatus,
        Integer remainingAmount,
        List<OrderItemSummaryResponse> orderItems,
        List<SplitPaymentDetailResponse> splits
) {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static OrderPaymentDetailResponse from(Order order,
                                                  List<Payment> payments,
                                                  List<PaymentResponseDto> paymentResponses) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(payments, "payments must not be null");
        Objects.requireNonNull(paymentResponses, "paymentResponses must not be null");

        if (payments.isEmpty()) {
            throw new IllegalArgumentException("payments must not be empty");
        }

        if (payments.size() != paymentResponses.size()) {
            throw new IllegalArgumentException("payments size and paymentResponses size must match");
        }

        List<Payment> sortedPayments = payments.stream()
                .sorted(Comparator.comparing(OrderPaymentDetailResponse::sortKey).reversed())
                .toList();

        int totalPaymentAmount = sortedPayments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        String orderStatus = order.getStatus() != null ? order.getStatus().name() : null;

        var responseByPaymentId = new java.util.HashMap<Long, PaymentResponseDto>();
        for (int i = 0; i < payments.size(); i++) {
            responseByPaymentId.put(payments.get(i).getId(), paymentResponses.get(i));
        }

        List<SplitPaymentDetailResponse> splitDetails = sortedPayments.stream()
                .map(payment -> SplitPaymentDetailResponse.from(payment, responseByPaymentId.get(payment.getId()), orderStatus))
                .collect(Collectors.toList());

        SplitPaymentDetailResponse representative = splitDetails.get(0);

        List<OrderItemSummaryResponse> orderItems = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .filter(Objects::nonNull)
                .map(OrderItemSummaryResponse::fromEntity)
                .toList()
                : List.of();

        Integer tableNumber = order.getRestaurantTable() != null
                ? order.getRestaurantTable().getTableNumber()
                : null;

        return new OrderPaymentDetailResponse(
                order.getOrderId(),
                totalPaymentAmount,
                splitDetails.size(),
                representative.paymentMethod(),
                representative.paymentStatus(),
                representative.approvedAt(),
                tableNumber,
                orderStatus,
                order.getRemainingAmount(),
                orderItems,
                splitDetails
        );
    }

    private static ZonedDateTime sortKey(Payment payment) {
        if (payment.getApprovedAt() != null) {
            return payment.getApprovedAt().atZoneSameInstant(DEFAULT_ZONE);
        }
        return payment.getCreatedAt().atZoneSameInstant(DEFAULT_ZONE);
    }
}
