package com.servease.demo.dto.response;

import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.CashPaymentRefund;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.model.entity.PaymentCancellation;
import com.servease.demo.model.enums.RepresentativePaymentDetailStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Locale;

public record SplitPaymentDetailResponse(
        Long paymentId,
        String paymentKey,
        Integer paymentAmount,
        Integer vat,
        String paymentMethod,
        String paymentStatus,
        RepresentativePaymentDetailStatus representativePaymentDetailStatus,
        String representativePaymentDetailStatusLabel,
        ZonedDateTime approvedAt,
        String approvalNumber,
        String approvalStatus
) {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static SplitPaymentDetailResponse from(Payment payment,
                                                  PaymentResponseDto paymentResponseDto,
                                                  String fallbackStatus,
                                                  PaymentCancellation paymentCancellation) {
        Objects.requireNonNull(payment, "payment must not be null");

        Integer amount = payment.getAmount();
        Integer vatAmount = amount != null ? Math.round(amount * 0.1f) : null;
        String method = payment.getMethod();
        String status = fallbackStatus;
        ZonedDateTime approvedAt = null;
        String approvalNumber = null;
        String approvalStatus = null;
        RepresentativePaymentDetailStatus displayStatus;

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

        if (paymentCancellation != null) {
            status = RepresentativePaymentDetailStatus.REFUNDED.name();
        }

        displayStatus = RepresentativePaymentDetailStatus.from(status);
        String paymentStatusForResponse = normalizePaymentStatus(status);

        return new SplitPaymentDetailResponse(
                payment.getId(),
                payment.getPaymentKey(),
                amount,
                vatAmount,
                method,
                paymentStatusForResponse,
                displayStatus,
                displayStatus.getLabel(),
                approvedAt,
                approvalNumber,
                approvalStatus
        );
    }

    public static SplitPaymentDetailResponse fromCash(CashPayment cashPayment,
                                                     String fallbackStatus,
                                                     CashPaymentRefund cashPaymentRefund) {
        Objects.requireNonNull(cashPayment, "cashPayment must not be null");

        Integer amount = cashPayment.getAmount();
        Integer vatAmount = amount != null ? Math.round(amount * 0.1f) : null;
        ZonedDateTime approvedAt = null;
        if (cashPayment.getReceivedAt() != null) {
            approvedAt = cashPayment.getReceivedAt().atZoneSameInstant(DEFAULT_ZONE);
        }

        String approvalNumber = String.valueOf(cashPayment.getId());

        boolean refunded = cashPaymentRefund != null;

        RepresentativePaymentDetailStatus displayStatus = refunded
                ? RepresentativePaymentDetailStatus.REFUNDED
                : RepresentativePaymentDetailStatus.PAID;

        String paymentStatusForResponse = refunded
                ? RepresentativePaymentDetailStatus.REFUNDED.name()
                : normalizePaymentStatus(fallbackStatus);

        return new SplitPaymentDetailResponse(
                cashPayment.getId(),
                approvalNumber,
                amount,
                vatAmount,
                "CASH",
                paymentStatusForResponse,
                displayStatus,
                displayStatus.getLabel(),
                approvedAt,
                approvalNumber,
                null
        );
    }

    private static String normalizePaymentStatus(String status) {
        if (status == null) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("PARTIALLY_PAID".equals(normalized) || "PARTIALLY_REFUNDED".equals(normalized)) {
            return "DONE";
        }
        return normalized;
    }
}
