package com.servease.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CashPaymentRequest(
        @NotNull
        @Positive
        Integer amount
) {
}
