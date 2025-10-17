package com.servease.demo.service.settlement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {

    @Transactional
    public void settleOrder(Long orderId) {
        // 정산 연동시 구현
    }
}
