package com.servease.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

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

    @Transactional
    public void recordCashRefund(Long orderId,
                                 Long storeId,
                                 Integer refundAmount,
                                 OffsetDateTime refundedAt) {
        if (storeId == null || refundAmount == null) {
            log.warn("Skip refund settlement. Missing storeId/refundAmount orderId={}, storeId={}, refundAmount={}",
                    orderId, storeId, refundAmount);
            return;
        }

        try {
            salesAggregationService.recordRefund(storeId, refundedAt, refundAmount);
        } catch (Exception e) {
            log.error("Failed to record refund settlement orderId={}, storeId={}", orderId, storeId, e);
            throw e;
        }
    }
}
