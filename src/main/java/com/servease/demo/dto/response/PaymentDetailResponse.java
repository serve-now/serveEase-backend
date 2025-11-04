package com.servease.demo.dto.response;

import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public record PaymentDetailResponse(
        Long paymentId,
        String paymentKey,
        Integer totalPaymentAmount,
        Integer paymentAmount,
        Integer vat,
        Integer remainingAmount,
        String paymentMethod,
        String paymentStatus,
        ZonedDateTime approvedAt,
        String approvalNumber,
        String approvalStatus,
        Integer tableNumber,
        List<OrderItemSummaryResponse> orderItems
) {

    public static PaymentDetailResponse from(Payment payment,
                                             Order order,
                                             PaymentResponseDto paymentResponseDto,
                                             List<OrderItemSummaryResponse> orderItemSummaries) {
        Objects.requireNonNull(payment, "payment가 null 입니다.");
        Objects.requireNonNull(order, "order가 null 입니다.");
        Objects.requireNonNull(orderItemSummaries, "orderItemSummaries 가 null 입니다.");

        Integer amount = payment.getAmount();
        Integer vatAmount = amount != null ? Math.round(amount * 0.1f) : null;

        PaymentResponseDto.Card card = paymentResponseDto != null ? paymentResponseDto.getCard() : null;
        String approvalNumber = null;
        String approvalStatus = null;
        String method = null;
        String status = null;
        ZonedDateTime approvedAt = null;

        if (paymentResponseDto != null) {
            method = paymentResponseDto.getMethod();
            status = paymentResponseDto.getStatus();
            if (paymentResponseDto.getApprovedAt() != null) {
                approvedAt = paymentResponseDto.getApprovedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul"));
            }
        }

        if (card != null) {
            approvalNumber = card.getApproveNo();
            approvalStatus = card.getAcquireStatus();
        }

        if (approvalNumber == null && paymentResponseDto != null) {
            approvalNumber = paymentResponseDto.getPaymentKey();
        }

        if (approvalNumber == null) {
            approvalNumber = payment.getPaymentKey();
        }

        if (method == null) {
            method = payment.getMethod();
        }

        if (approvedAt == null && payment.getApprovedAt() != null) {
            approvedAt = payment.getApprovedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul"));
        }

        Integer tableNumber = null;
        if (order.getRestaurantTable() != null) {
            tableNumber = order.getRestaurantTable().getTableNumber();
        }

        return new PaymentDetailResponse(
                payment.getId(),
                payment.getPaymentKey(),
                order.getTotalPrice(),
                amount,
                vatAmount,
                order.getRemainingAmount(),
                method,
                status,
                approvedAt,
                approvalNumber,
                approvalStatus,
                tableNumber,
                List.copyOf(orderItemSummaries)
        );
    }

    public static PaymentDetailResponse of(Payment payment, PaymentResponseDto paymentResponseDto) {
        Order order = payment.getOrder();
        List<OrderItemSummaryResponse> orderItemSummaries = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                        .map(OrderItemSummaryResponse::fromEntity)
                        .toList()
                : List.of();

        return from(payment, order, paymentResponseDto, orderItemSummaries);
    }
}
