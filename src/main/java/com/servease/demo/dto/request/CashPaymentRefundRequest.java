package com.servease.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CashPaymentRefundRequest(
        @NotNull
        Long cashPaymentId,

        @NotNull
        @Positive
        Integer refundAmount,

        @Size(max = 255)
        String refundReason
) {
}
