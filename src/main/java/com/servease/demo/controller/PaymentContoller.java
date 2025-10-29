package com.servease.demo.controller;

import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/payments")
public class PaymentContoller {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(@Valid @RequestBody TossConfirmRequest tossConfirmRequest) {
        log.info("[PAYMENT] confirm request orderId={}, paymentKey={}", tossConfirmRequest.orderId(), tossConfirmRequest.paymentKey());
        PaymentConfirmResponse response = paymentService.confirmAndSave(tossConfirmRequest);
        log.info("[PAYMENT] confirm success orderId={}, remainingAmount={}", tossConfirmRequest.orderId(), response.remainingAmount());
        return ResponseEntity.ok(response);
    }
}


