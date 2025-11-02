package com.servease.demo.dto.request;

import com.servease.demo.model.enums.PaymentMethodFilter;
import com.servease.demo.model.enums.PaymentOrderTypeFilter;
import com.servease.demo.model.enums.PaymentQuickRange;

import java.time.LocalDate;

public record PaymentSearchRequest(
        PaymentQuickRange quickRange,
        LocalDate from,
        LocalDate to,
        PaymentMethodFilter paymentMethod,
        PaymentOrderTypeFilter orderType
) {
}

