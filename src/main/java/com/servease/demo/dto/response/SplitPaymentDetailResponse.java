package com.servease.demo.dto.response;

import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.Payment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public record SplitPaymentDetailResponse(
        Long paymentId,
        String paymentKey,
        Integer paymentAmount,
        Integer vat,
        String paymentMethod,
        String paymentStatus,
        ZonedDateTime approvedAt,
        String approvalNumber,
        String approvalStatus
) {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static SplitPaymentDetailResponse from(Payment payment,
                                                  PaymentResponseDto paymentResponseDto,
                                                  String fallbackStatus) {
        Objects.requireNonNull(payment, "payment must not be null");

        Integer amount = payment.getAmount();
        Integer vatAmount = amount != null ? Math.round(amount * 0.1f) : null;
        String method = payment.getMethod();
        String status = fallbackStatus;
        ZonedDateTime approvedAt = null;
        String approvalNumber = null;
        String approvalStatus = null;

        if (payment.getApprovedAt() != null) {
            approvedAt = payment.getApprovedAt().atZoneSameInstant(DEFAULT_ZONE);
        }

        if (paymentResponseDto != null) {
            if (paymentResponseDto.getMethod() != null) {
                method = paymentResponseDto.getMethod();
            }
            if (paymentResponseDto.getStatus() != null) {
                status = paymentResponseDto.getStatus();
            }
            if (paymentResponseDto.getApprovedAt() != null) {
                approvedAt = paymentResponseDto.getApprovedAt().atZoneSameInstant(DEFAULT_ZONE);
            }

            PaymentResponseDto.Card card = paymentResponseDto.getCard();
            if (card != null) {
                approvalNumber = card.getApproveNo();
                approvalStatus = card.getAcquireStatus();
            }

            if (approvalNumber == null) {
                approvalNumber = paymentResponseDto.getPaymentKey();
            }
        }

        if (approvalNumber == null) {
            approvalNumber = payment.getPaymentKey();
        }

        return new SplitPaymentDetailResponse(
                payment.getId(),
                payment.getPaymentKey(),
                amount,
                vatAmount,
                method,
                status,
                approvedAt,
                approvalNumber,
                approvalStatus
        );
    }

    public static SplitPaymentDetailResponse fromCash(CashPayment cashPayment, String fallbackStatus) {
        Objects.requireNonNull(cashPayment, "cashPayment must not be null");

        Integer amount = cashPayment.getAmount();
        Integer vatAmount = amount != null ? Math.round(amount * 0.1f) : null;
        ZonedDateTime approvedAt = null;
        if (cashPayment.getReceivedAt() != null) {
            approvedAt = cashPayment.getReceivedAt().atZoneSameInstant(DEFAULT_ZONE);
        }

        String approvalNumber = String.valueOf(cashPayment.getId());

        return new SplitPaymentDetailResponse(
                cashPayment.getId(),
                approvalNumber,
                amount,
                vatAmount,
                "CASH",
                fallbackStatus,
                approvedAt,
                approvalNumber,
                null
        );
    }
}
