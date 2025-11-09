package com.servease.demo.service.event;

import java.time.OffsetDateTime;

public record OrderRefundedEvent(
        Long orderId,
        Long storeId,
        Integer refundAmount,
        OffsetDateTime refundedAt
) {
}
