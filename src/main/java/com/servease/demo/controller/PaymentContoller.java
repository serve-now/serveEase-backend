package com.servease.demo.controller;

import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.dto.response.PaymentDetailResponse;
import com.servease.demo.dto.response.PaymentListResponse;
import com.servease.demo.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentContoller {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(@Valid @RequestBody TossConfirmRequest tossConfirmRequest) {
        return ResponseEntity.ok(paymentService.confirmAndSave(tossConfirmRequest));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentListResponse>> getPayments(
            @PageableDefault(sort = "approvedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(paymentService.getPayments(pageable));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponse> getPaymentDetails(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentDetail(paymentId));
    }
}
