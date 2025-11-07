package com.servease.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PaymentCancelRequest(
        @NotBlank
        @Size(max = 200)
        String paymentKey,

        @NotNull
        @Positive
        Integer cancelAmount
) {
}
