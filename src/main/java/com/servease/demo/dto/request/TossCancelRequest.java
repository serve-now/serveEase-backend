package com.servease.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;

public record TossCancelRequest(
        @Positive
        Integer cancelAmount
) {
    private static final String DEFAULT_CANCEL_REASON = "단순 변심";

    @JsonProperty("cancelReason")
    public String cancelReason() {
        return DEFAULT_CANCEL_REASON;
    }
}
