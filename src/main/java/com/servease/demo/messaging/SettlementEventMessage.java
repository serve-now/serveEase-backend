package com.servease.demo.messaging;

import com.servease.demo.service.event.OrderFullyPaidEvent;
import com.servease.demo.service.event.OrderRefundedEvent;
import java.time.OffsetDateTime;

public record SettlementEventMessage(
        SettlementEventType type,
        Long orderId,
        Long storeId,
        Integer refundAmount,
        OffsetDateTime refundedAt
) {

    public static SettlementEventMessage from(OrderFullyPaidEvent event) {
        return new SettlementEventMessage(
                SettlementEventType.ORDER_FULLY_PAID,
                event.orderId(),
                null,
                null,
                null
        );
    }

    public static SettlementEventMessage from(OrderRefundedEvent event) {
        return new SettlementEventMessage(
                SettlementEventType.ORDER_REFUNDED,
                event.orderId(),
                event.storeId(),
                event.refundAmount(),
                event.refundedAt()
        );
    }
}
