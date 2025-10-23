package com.servease.demo.dto.response;

import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public record PaymentConfirmResponse(
        String paymentOrderId,
        String orderId,
        String method,
        String cardCompany,        // 카드 아니면 null
        String maskedCardNumber,   // 카드 아니면 null
        String approvalNumber,     // 카드: approveNo, 그 외 결제수단: paymentKey
        String cashApprovalNumber, // 현금 결제 시 영수증/전표 번호용
        ZonedDateTime approvedAt,
        Integer paidAmount,
        Integer remainingAmount,
        OrderStatus orderStatus
) {
    public static PaymentConfirmResponse from(PaymentResponseDto dto, Order order) {
        PaymentResponseDto.Card card = dto.getCard();
        String issuerCode = card != null ? card.getIssuerCode() : null;
        String maskedNumber = card != null ? card.getNumber() : null;
        String approvalNumber = card != null ? card.getApproveNo() : dto.getPaymentKey();

        ZonedDateTime approvedAt = dto.getApprovedAt() != null
                ? dto.getApprovedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                : null;

        return new PaymentConfirmResponse(
                dto.getOrderId(),
                order.getOrderId(),
                dto.getMethod(),
                issuerCode,
                maskedNumber,
                approvalNumber,
                null,
                approvedAt,
                order.getPaidAmount(),
                order.getRemainingAmount(),
                order.getStatus()
        );
    }

    public static PaymentConfirmResponse fromCashPayment(CashPayment cashPayment, Order order) {
        ZonedDateTime receivedAt = cashPayment.getReceivedAt() != null
                ? cashPayment.getReceivedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                : null;

        return new PaymentConfirmResponse(
                order.getOrderId(),
                order.getOrderId(),
                "CASH",
                null,
                null,
                null,
                String.valueOf(cashPayment.getId()),
                receivedAt,
                order.getPaidAmount(),
                order.getRemainingAmount(),
                order.getStatus()
        );
    }
}
