package com.servease.demo.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TossCancelResponse {
    private String mId;
    private String lastTransactionKey;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Integer taxExemptionAmount;
    private String status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private String currency;
    private Integer totalAmount;
    private EasyPay easyPay;
    private List<CancelHistory> cancels;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EasyPay {
        private String provider;
        private Integer amount;
        private Integer discountAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CancelHistory {
        private String transactionKey;
        private String cancelReason;
        private Integer cancelAmount;
        private OffsetDateTime canceledAt;
        private String cancelStatus;
    }
}
