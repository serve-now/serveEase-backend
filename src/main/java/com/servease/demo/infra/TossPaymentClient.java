package com.servease.demo.infra;

import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.dto.request.TossCancelRequest;
import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.TossCancelResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class TossPaymentClient {
    private final RestTemplate restTemplate;
    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    public TossPaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentResponseDto confirm(TossConfirmRequest request) {
        TossConfirmPayload payload = new TossConfirmPayload(request.paymentKey(), request.orderId(), request.amount());
        ResponseEntity<PaymentResponseDto> res = restTemplate.postForEntity(TOSS_CONFIRM_URL, payload, PaymentResponseDto.class);
        if (!res.getStatusCode().is2xxSuccessful()) {
            log.error("Toss confirm failed. Response: {}", res.getBody());
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }

        return res.getBody();

    }

    private record TossConfirmPayload(String paymentKey, String orderId, Integer amount) {
    }

    public TossCancelResponse cancel(String paymentKey, TossCancelRequest request) {
        ResponseEntity<TossCancelResponse> res = restTemplate.postForEntity(
                TOSS_CANCEL_URL,
                request,
                TossCancelResponse.class,
                paymentKey
        );

        if (!res.getStatusCode().is2xxSuccessful()) {
            log.error("Toss cancel failed. paymentKey={}, cancelAmount={}, reason={}", paymentKey, request.cancelAmount(), request.cancelReason());
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_CANCEL_FAILED);
        }

        return res.getBody();
    }
}
