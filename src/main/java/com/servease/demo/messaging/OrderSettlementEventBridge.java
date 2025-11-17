package com.servease.demo.messaging;

import com.servease.demo.config.SettlementEventBrokerProperties;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import com.servease.demo.service.event.OrderRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSettlementEventBridge {

    private final RabbitTemplate rabbitTemplate;
    private final SettlementEventBrokerProperties properties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishOrderFullyPaid(OrderFullyPaidEvent event) {
        publish(SettlementEventMessage.from(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishOrderRefunded(OrderRefundedEvent event) {
        publish(SettlementEventMessage.from(event));
    }

    private void publish(SettlementEventMessage message) {
        rabbitTemplate.convertAndSend(
                properties.getExchange(),
                properties.getRoutingKey(),
                message
        );
        log.info("Published settlement event {} for order {}", message.type(), message.orderId());
    }
}
