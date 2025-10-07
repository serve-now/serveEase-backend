package com.servease.demo.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.repository.OrderRepository;
import com.servease.demo.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository; // OrderRepository 주입
    private final OrderService orderService;
    private final ObjectMapper om = new ObjectMapper();

    // 사용자가 결제창에서 인증 끝내고 successUrl로 돌아온 순간에 paymentKey, orderId, amount를 서버에 저장
    // 같은트랜잭션으로 saveAndConfirm 바로 호출
    // 10분 넘기면 EXPIRED/NOT_FOUND_PAYMENT_SESSION으로 실패 (toss에서)

    @Transactional
    public PaymentConfirmResponse confirmAndSave(TossConfirmRequest tossConfirmRequest) {
        //응답에서 orderId 추출,  order 찾기 (completePayment 호출 위함)
        String orderId = tossConfirmRequest.orderId();
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다. orderId: " + orderId));

        if (!order.getTotalPrice().equals(tossConfirmRequest.amount())) {
            throw new BusinessException(ErrorCode.AMOUNT_NOT_MATCH, "주문 금액과 결제 금액이 일치하지 않습니다.");
        }

        if (order.isPaid()) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }

        // 토스 페이먼트에 결제 승인 요청(raw 문자열로 받기)
        var body = Map.of("paymentKey", tossConfirmRequest.paymentKey(),
                "orderId", tossConfirmRequest.orderId(),
                "amount", tossConfirmRequest.amount()
        );

        ResponseEntity<String> res;
        try {
            res = restTemplate.postForEntity("https://api.tosspayments.com/v1/payments/confirm", body, String.class);
        } catch (HttpStatusCodeException e) {
            log.error("Toss confirm failed. Response: {}", e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }

        String rawJson = res.getBody();

        // JSON 트리로 파싱 (중간 DTO 없이 그냥 받음)
        JsonNode root;
        try {
            root = om.readTree(rawJson);
        } catch (Exception e) {
            log.error("Failed to parse Toss payment response JSON. rawJson: {}", rawJson, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "결제 응답 처리 중 오류가 발생했습니다.");
        }


        //응답에서 paymentKey 추출
        String paymentKey = root.path("paymentKey").asText(tossConfirmRequest.paymentKey()); // 파싱실패 대비 폴백

        //저장 및 order.isPaid= true
        paymentRepository.save(Payment.from(paymentKey, orderId, tossConfirmRequest.amount(), rawJson));
        orderService.completePayment(order.getId());

        // 프론트 응답 DTO 만들기
        return PaymentConfirmResponse.from(root);
    }
}

