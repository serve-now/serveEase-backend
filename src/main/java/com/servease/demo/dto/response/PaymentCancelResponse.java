package com.servease.demo.dto.response;

import com.servease.demo.model.enums.OrderStatus;

import java.time.ZonedDateTime;

public record PaymentCancelResponse(
        String paymentKey,
        String orderId,
        Integer canceledAmount,
        Integer paidAmount,
        Integer remainingAmount,
        OrderStatus orderStatus,
        ZonedDateTime canceledAt,
        String cancelReason
) {
}
