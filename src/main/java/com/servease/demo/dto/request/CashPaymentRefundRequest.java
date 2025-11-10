package com.servease.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record CashPaymentRefundRequest(
        @NotNull
        @Positive
        Integer refundAmount
) {
}
