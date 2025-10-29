package com.servease.demo.service.event;

import com.servease.demo.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderSettlementEventHandler {

    private final SettlementService settlementService;

    @EventListener()
    public void handleOrderFullyPaid(OrderFullyPaidEvent event) {
        settlementService.settleOrder(event.orderId());
    }
}
