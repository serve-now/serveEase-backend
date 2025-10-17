package com.servease.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResponseDto {

    private String mId;
    private String lastTransactionKey;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Integer taxExemptionAmount;
    private String status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private Boolean useEscrow;
    private Boolean cultureExpense;
    private Card card;
    private Object virtualAccount;
    private Object transfer;
    private Object mobilePhone;
    private Object giftCertificate;
    private Object cashReceipt;
    private Object cashReceipts;
    private Object discount;
    private Object cancels;
    private Object secret;
    private String type;
    private EasyPay easyPay;
    private String country;
    private Object failure;
    private Boolean isPartialCancelable;
    private Receipt receipt;
    private Checkout checkout;
    private String currency;
    private Integer totalAmount;
    private Integer balanceAmount;
    private Integer suppliedAmount;
    private Integer vat;
    private Integer taxFreeAmount;
    private Object metadata;
    private String method;
    private String version;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Card {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private Integer installmentPlanMonths;
        private Boolean isInterestFree;
        private Object interestPayer;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private Integer amount;
    }

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
    public static class Receipt {
        private String url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Checkout {
        private String url;
    }
}
