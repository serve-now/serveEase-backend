package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.model.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static PaymentCancelResponse from(Payment payment,
                                             Order order,
                                             int canceledAmount,
                                             String cancelReason,
                                             OffsetDateTime canceledAt) {
        return from(payment, order, canceledAmount, cancelReason, canceledAt, DEFAULT_ZONE);
    }

    public static PaymentCancelResponse from(Payment payment,
                                             Order order,
                                             int canceledAmount,
                                             String cancelReason,
                                             OffsetDateTime canceledAt,
                                             ZoneId zoneId) {
        ZonedDateTime canceledAtZone = canceledAt != null
                ? canceledAt.atZoneSameInstant(zoneId)
                : ZonedDateTime.now(zoneId);

        return new PaymentCancelResponse(
                payment.getPaymentKey(),
                order.getOrderId(),
                canceledAmount,
                order.getPaidAmount(),
                order.getRemainingAmount(),
                order.getStatus(),
                canceledAtZone,
                cancelReason
        );
    }
}
