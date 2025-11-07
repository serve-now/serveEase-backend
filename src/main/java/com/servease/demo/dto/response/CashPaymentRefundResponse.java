package com.servease.demo.dto.response;

import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.CashPaymentRefund;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record CashPaymentRefundResponse(
        Long cashPaymentId,
        String orderId,
        Integer refundedAmount,
        Integer paidAmount,
        Integer remainingAmount,
        OrderStatus orderStatus,
        ZonedDateTime refundedAt,
        String refundReason
) {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static CashPaymentRefundResponse from(Order order,
                                                 CashPayment cashPayment,
                                                 CashPaymentRefund refund) {
        ZonedDateTime refundedAt = resolveRefundedAt(refund.getRefundedAt());

        return new CashPaymentRefundResponse(
                cashPayment.getId(),
                order.getOrderId(),
                refund.getRefundAmount(),
                order.getPaidAmount(),
                order.getRemainingAmount(),
                order.getStatus(),
                refundedAt,
                refund.getRefundReason()
        );
    }

    private static ZonedDateTime resolveRefundedAt(OffsetDateTime refundedAt) {
        if (refundedAt == null) {
            return OffsetDateTime.now().atZoneSameInstant(DEFAULT_ZONE);
        }
        return refundedAt.atZoneSameInstant(DEFAULT_ZONE);
    }
}
