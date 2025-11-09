package com.servease.demo.service.event;

import com.servease.demo.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderSettlementEventHandler {

    private final SettlementService settlementService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderFullyPaid(OrderFullyPaidEvent event) {
        settlementService.settleOrder(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderRefunded(OrderRefundedEvent event) {
        settlementService.recordRefund(event.orderId(), event.storeId(), event.refundAmount(), event.refundedAt());
    }
}
