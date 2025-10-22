package com.servease.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SalesAggregationService salesAggregationService;

    @Transactional
    public void settleOrder(Long orderId) {
        try {
            salesAggregationService.upsertDaily(orderId);
        } catch (Exception e) {
            log.error("Failed to settle order {}", orderId, e);
            throw e;
        }
    }
}
