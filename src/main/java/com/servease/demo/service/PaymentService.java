package com.servease.demo.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@AllArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper om = new ObjectMapper();

    // 사용자가 결제창에서 인증 끝내고 successUrl로 돌아온 순간에 paymentKey, orderId, amount를 서버에 저장
    // 같은트랜잭션으로 saveAndConfirm 바로 호출
    // 10분 넘기면 EXPIRED/NOT_FOUND_PAYMENT_SESSION으로 실패 (toss에서)

    @Transactional
    public PaymentConfirmResponse confirmAndSave(TossConfirmRequest tossConfirmRequest) {
        // 토스 Confirm 호출 (raw 문자열로 받기)
        var body = Map.of("paymentKey", tossConfirmRequest.paymentKey(),
                "orderId", tossConfirmRequest.orderId(),
                "amount", tossConfirmRequest.amount()
        );
        ResponseEntity<String> res;
        try {
            res = restTemplate.postForEntity("https://api.tosspayments.com/v1/payments/confirm", body, String.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Toss confirm failed: " + e.getResponseBodyAsString(), e);
        }

        String rawJson = res.getBody();

        // JSON 트리로 파싱 (중간 DTO 없이 그냥 받음)
        JsonNode root;
        try {
            root = om.readTree(rawJson);
        } catch (Exception e) {
            throw new RuntimeException("Parse error", e);
        }

        // 저장 : 응답에서 paymentKey/orderId 추출
        String paymentKey = root.path("paymentKey").asText(tossConfirmRequest.paymentKey()); // 파싱실패 대비 폴백
        String orderId    = root.path("orderId").asText(tossConfirmRequest.orderId());

        paymentRepository.save(Payment.from(paymentKey, orderId, tossConfirmRequest.amount(), rawJson));

        // 프론트 응답 DTO 만들기
        return PaymentConfirmResponse.from(root);
    }
}

