package com.servease.demo.controller;

import com.servease.demo.dto.request.PaymentCancelRequest;
import com.servease.demo.dto.request.PaymentSearchRequest;
import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.OrderPaymentDetailResponse;
import com.servease.demo.dto.response.OrderPaymentListResponse;
import com.servease.demo.dto.response.PaymentCancelResponse;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.enums.PaymentMethodFilter;
import com.servease.demo.model.enums.PaymentOrderTypeFilter;
import com.servease.demo.model.enums.PaymentQuickRange;
import com.servease.demo.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Locale;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/payments")
public class PaymentContoller {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public PaymentConfirmResponse confirm(@Valid @RequestBody TossConfirmRequest tossConfirmRequest) {
        log.info("[PAYMENT] confirm request orderId={}, paymentKey={}", tossConfirmRequest.orderId(), tossConfirmRequest.paymentKey());
        PaymentConfirmResponse response = paymentService.confirmAndSave(tossConfirmRequest);
        log.info("[PAYMENT] confirm success orderId={}, remainingAmount={}", tossConfirmRequest.orderId(), response.remainingAmount());
        return response;
    }

    @PostMapping("/cancel")
    public PaymentCancelResponse cancel(@Valid @RequestBody PaymentCancelRequest paymentCancelRequest) {
        log.info("[PAYMENT] cancel request paymentKey={}, cancelAmount={}", paymentCancelRequest.paymentKey(), paymentCancelRequest.cancelAmount());
        PaymentCancelResponse response = paymentService.cancel(paymentCancelRequest);
        log.info("[PAYMENT] cancel success paymentKey={}, canceledAmount={}, remainingAmount={}",
                response.paymentKey(), response.canceledAmount(), response.remainingAmount());
        return response;
    }

    @GetMapping
    public Page<OrderPaymentListResponse> getPayments(
            @PageableDefault(sort = "approvedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "range", required = false) String range,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "orderType", required = false) String orderType
    ) {
        PaymentSearchRequest searchRequest = new PaymentSearchRequest(
                parseEnum(range, PaymentQuickRange.class, "range"),
                from,
                to,
                parseEnum(paymentMethod, PaymentMethodFilter.class, "paymentMethod"),
                parseEnum(orderType, PaymentOrderTypeFilter.class, "orderType")
        );

        return paymentService.getPayments(pageable, searchRequest);
    }

    @GetMapping("/{paymentId}")
    public OrderPaymentDetailResponse getPaymentDetails(@PathVariable Long paymentId) {
        return paymentService.getPaymentDetail(paymentId);
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String parameterName) {
        //문자열이 null 필터 적용x (null 반환)
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    String.format("지원하지 않는 %s 값입니다: %s", parameterName, value)
            );
        }
    }
}
