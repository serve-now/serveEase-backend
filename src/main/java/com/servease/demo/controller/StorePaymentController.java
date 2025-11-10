package com.servease.demo.controller;

import com.servease.demo.dto.request.CashPaymentRefundRequest;
import com.servease.demo.dto.response.CashPaymentRefundResponse;
import com.servease.demo.service.CashPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores/{storeId}/payments")
@RequiredArgsConstructor
@Slf4j
public class StorePaymentController {

    private final CashPaymentService cashPaymentService;

    @PostMapping("/{cashPaymentId}/cash/refund")
    public ResponseEntity<CashPaymentRefundResponse> refundCashPayment(
            @PathVariable Long storeId,
            @PathVariable Long cashPaymentId,
            @Valid @RequestBody CashPaymentRefundRequest request
    ) {
        log.info("[PAYMENT] cash refund request storeId={}, cashPaymentId={}, amount={}",
                storeId, cashPaymentId, request.refundAmount());
        CashPaymentRefundResponse response = cashPaymentService.refundCashPayment(cashPaymentId, request);
        log.info("[PAYMENT] cash refund success storeId={}, cashPaymentId={}, paidAmount={}, remainingAmount={}",
                storeId, cashPaymentId, response.paidAmount(), response.remainingAmount());
        return ResponseEntity.ok(response);
    }
}
