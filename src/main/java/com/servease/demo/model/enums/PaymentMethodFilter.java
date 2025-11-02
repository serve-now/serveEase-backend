package com.servease.demo.model.enums;

import java.util.Set;

public enum PaymentMethodFilter {
    CARD(Set.of("CARD", "EASY_PAY")),
    CASH(Set.of("CASH")),
    EASY_PAY(Set.of("EASY_PAY"));

    private final Set<String> acceptedMethods;

    PaymentMethodFilter(Set<String> acceptedMethods) {
        this.acceptedMethods = acceptedMethods;
    }

    public Set<String> acceptedMethods() {
        return acceptedMethods;
    }
}
