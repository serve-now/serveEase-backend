package com.servease.demo.model.enums;

import java.util.Locale;

public enum PaymentDisplayStatus {

    PAID("결제 완료"),
    CANCELED("결제 취소"),
    CONFIRMATION_REQUIRED("확인 필요");

    private final String label;

    PaymentDisplayStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PaymentDisplayStatus from(String paymentStatus) {
        String normalized = paymentStatus == null
                ? ""
                : paymentStatus.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "DONE", "COMPLETED" -> PAID;
            case "REFUNDED" -> CANCELED;
            default -> CONFIRMATION_REQUIRED;
        };
    }
}
