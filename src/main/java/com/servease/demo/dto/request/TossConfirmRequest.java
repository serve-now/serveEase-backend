package com.servease.demo.dto.request;
import jakarta.validation.constraints.*;

public record TossConfirmRequest(
        @NotBlank
        @Size(max = 200)
        String paymentKey,

        @NotBlank @Size(min = 6, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9_-]{6,64}$")
        String orderId,

        @NotNull
        @Positive
        Integer amount
) {

}
