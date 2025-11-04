package com.servease.demo.model.enums;

import java.util.Set;

public enum PaymentMethodFilter {
    CARD(Set.of("CARD", "EASY_PAY", "카드", "간편결제")),
    CASH(Set.of("CASH", "현금")),
    EASY_PAY(Set.of("EASY_PAY", "간편결제"));

    private final Set<String> acceptedMethods;

    PaymentMethodFilter(Set<String> acceptedMethods) {
        this.acceptedMethods = acceptedMethods;
    }

    public Set<String> acceptedMethods() {
        return acceptedMethods;
    }
}
