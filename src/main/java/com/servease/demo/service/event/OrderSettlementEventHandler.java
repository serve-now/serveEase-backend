package com.servease.demo.service.event;

import com.servease.demo.service.settlement.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderSettlementEventHandler {

    private final SettlementService settlementService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderFullyPaid(OrderFullyPaidEvent event) {
        settlementService.settleOrder(event.orderId());
    }
}
