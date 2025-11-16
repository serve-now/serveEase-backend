package com.servease.demo.messaging;

import static org.mockito.Mockito.verify;

import com.servease.demo.service.SettlementService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class OrderSettlementEventConsumerTest {

    @Mock
    private SettlementService settlementService;

    private OrderSettlementEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderSettlementEventConsumer(settlementService);
    }

    @Test
    void consume_fullyPaid_invokesSettlement() {
        SettlementEventMessage message = new SettlementEventMessage(
                SettlementEventType.ORDER_FULLY_PAID,
                9L,
                null,
                null,
                null
        );

        consumer.consume(message);

        verify(settlementService).settleOrder(9L);
    }

    @Test
    void consume_refunded_invokesRecordRefund() {
        OffsetDateTime refundedAt = OffsetDateTime.now();
        SettlementEventMessage message = new SettlementEventMessage(
                SettlementEventType.ORDER_REFUNDED,
                12L,
                5L,
                3_000,
                refundedAt
        );

        consumer.consume(message);

        verify(settlementService).recordRefund(12L, 5L, 3_000, refundedAt);
    }
}
