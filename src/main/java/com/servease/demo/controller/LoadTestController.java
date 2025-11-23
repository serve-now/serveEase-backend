package com.servease.demo.controller;

import com.servease.demo.service.SettlementService;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/load-test")
@RequiredArgsConstructor
public class LoadTestController {

    private final ApplicationEventPublisher eventPublisher;
    private final SettlementService settlementService;

    @GetMapping("/blocking")
    public void testByBlocking(
            @RequestParam("orderId") Long orderId
    ) {
        settlementService.settleOrder(orderId);
    }

    @RequestMapping("/event")
    public void testByEvent(
            @RequestParam("orderId") Long orderId
    ) {
        eventPublisher.publishEvent(new OrderFullyPaidEvent(orderId));
    }
}
