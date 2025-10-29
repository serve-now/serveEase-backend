package com.servease.demo;

import com.servease.demo.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ManualSettlementTest {

    @Autowired
    private SettlementService settlementService;

    @Test
    void settleOrder105() {
        settlementService.settleOrder(105L);
    }
}
