package com.servease.demo.dto.response;

import com.servease.demo.dto.PaymentResponseDto;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public record PaymentConfirmResponse(
        String orderId,
        String method,
        String cardCompany,        // 카드 아니면 null
        String maskedCardNumber,   // 카드 아니면 null
        String approvalNumber,     // 카드: approveNo, 그 외: paymentKey
        ZonedDateTime approvedAt
) {
    public static PaymentConfirmResponse from(PaymentResponseDto dto) {
        return new PaymentConfirmResponse(
                dto.getOrderId(),
                dto.getMethod(),
                dto.getCard().getIssuerCode(),
                dto.getCard().getApproveNo(),
                dto.getCard().getNumber(),
                dto.getApprovedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul"))
        );
    }
}
