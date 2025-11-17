package com.servease.demo.messaging;

import com.servease.demo.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSettlementEventConsumer {

    private final SettlementService settlementService;

    @RabbitListener(queues = "${settlement.broker.queue}", containerFactory = "settlementListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void consume(SettlementEventMessage message) {
        log.info("Consuming settlement event {} for order {}", message.type(), message.orderId());
        switch (message.type()) {
            case ORDER_FULLY_PAID -> handleFullyPaid(message);
            case ORDER_REFUNDED -> handleRefunded(message);
        }
    }

    private void handleFullyPaid(SettlementEventMessage message) {
        settlementService.settleOrder(message.orderId());
    }

    private void handleRefunded(SettlementEventMessage message) {
        settlementService.recordRefund(
                message.orderId(),
                message.storeId(),
                message.refundAmount(),
                message.refundedAt()
        );
    }
}
