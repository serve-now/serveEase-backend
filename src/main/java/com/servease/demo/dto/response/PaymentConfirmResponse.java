package com.servease.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentConfirmResponse(
        String orderId,
        String method,
        String cardCompany,        // 카드 아니면 null
        String maskedCardNumber,   // 카드 아니면 null
        String approvalNumber,     // 카드: approveNo, 그 외: paymentKey
        OffsetDateTime approvedAt
) {
    public static PaymentConfirmResponse from(JsonNode root) {
        String orderId = text(root, "orderId");
        String method  = text(root, "method");
        String paymentKey = text(root, "paymentKey");
        String approvedAtRaw = text(root, "approvedAt");
        OffsetDateTime approvedAt = (approvedAtRaw != null && !approvedAtRaw.isBlank())
                ? OffsetDateTime.parse(approvedAtRaw) : null;

        JsonNode card = root.path("card");
        String cardCompany = card.isMissingNode() ? null : text(card, "company");
        String maskedCard  = card.isMissingNode() ? null : text(card, "number");
        String approveNo   = card.isMissingNode() ? null : text(card, "approveNo");

        String approvalNumber = (approveNo != null && !approveNo.isBlank()) ? approveNo : paymentKey;

        return new PaymentConfirmResponse(orderId, method, cardCompany, maskedCard, approvalNumber, approvedAt);
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }
}
