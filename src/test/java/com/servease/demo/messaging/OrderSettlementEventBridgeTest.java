package com.servease.demo.messaging;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.servease.demo.config.SettlementEventBrokerProperties;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import com.servease.demo.service.event.OrderRefundedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class OrderSettlementEventBridgeTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private SettlementEventBrokerProperties properties;

    @InjectMocks
    private OrderSettlementEventBridge bridge;

    @BeforeEach
    void setUp() {
        properties = new SettlementEventBrokerProperties();
        properties.setExchange("test.exchange");
        properties.setRoutingKey("test.key");
        properties.setQueue("test.queue");
        bridge = new OrderSettlementEventBridge(rabbitTemplate, properties);
    }

    @Test
    void publishOrderFullyPaid_sendsMessageToBroker() {
        OrderFullyPaidEvent event = new OrderFullyPaidEvent(10L);
        SettlementEventMessage expected = SettlementEventMessage.from(event);

        bridge.publishOrderFullyPaid(event);

        verify(rabbitTemplate).convertAndSend(
                eq(properties.getExchange()),
                eq(properties.getRoutingKey()),
                eq(expected)
        );
    }

    @Test
    void publishOrderRefunded_sendsMessageToBroker() {
        OrderRefundedEvent event = new OrderRefundedEvent(11L, 3L, 1_000, null);
        SettlementEventMessage expected = SettlementEventMessage.from(event);

        bridge.publishOrderRefunded(event);

        verify(rabbitTemplate).convertAndSend(
                eq(properties.getExchange()),
                eq(properties.getRoutingKey()),
                eq(expected)
        );
    }
}
