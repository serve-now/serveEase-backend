package com.servease.demo.dto.response;

import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.CashPaymentRefund;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.model.entity.PaymentCancellation;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.model.enums.RepresentativePaymentDetailStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record OrderPaymentDetailResponse(
        String orderId,
        Integer totalPaymentAmount,
        Integer splitCount,
        String representativePaymentMethod,
        String representativePaymentStatus,
        ZonedDateTime representativeApprovedAt,
        Integer tableNumber,
        String orderStatus,
        Integer remainingAmount,
        List<OrderItemSummaryResponse> orderItems,
        List<SplitPaymentDetailResponse> splits
) {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    public static OrderPaymentDetailResponse from(Order order,
                                                  List<Payment> payments,
                                                  List<PaymentResponseDto> paymentResponses,
                                                  List<CashPayment> cashPayments,
                                                  Map<Long, PaymentCancellation> paymentCancellationByPaymentId,
                                                  Map<Long, CashPaymentRefund> cashRefundByPaymentId) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(payments, "payments must not be null");
        Objects.requireNonNull(paymentResponses, "paymentResponses must not be null");
        Objects.requireNonNull(cashPayments, "cashPayments must not be null");
        Objects.requireNonNull(paymentCancellationByPaymentId, "paymentCancellationByPaymentId must not be null");
        Objects.requireNonNull(cashRefundByPaymentId, "cashRefundByPaymentId must not be null");

        if (payments.size() != paymentResponses.size()) {
            throw new IllegalArgumentException("payments size and paymentResponses size must match");
        }

        if (payments.isEmpty() && cashPayments.isEmpty()) {
            throw new IllegalArgumentException("payments or cashPayments must not be empty");
        }

        List<Payment> sortedPayments = payments.stream()
                .sorted(Comparator.comparing((Payment payment) -> sortKey(payment)).reversed())
                .toList();
        OrderStatus orderStatusEnum = order.getStatus();
        String orderStatus = orderStatusEnum != null ? orderStatusEnum.name() : null;

        var responseByPaymentId = new java.util.HashMap<Long, PaymentResponseDto>();
        for (int i = 0; i < payments.size(); i++) {
            responseByPaymentId.put(payments.get(i).getId(), paymentResponses.get(i));
        }

        List<SplitEntry> splitEntries = new ArrayList<>(payments.size() + cashPayments.size());

        for (Payment payment : sortedPayments) {
            SplitPaymentDetailResponse detail = SplitPaymentDetailResponse.from(
                    payment,
                    responseByPaymentId.get(payment.getId()),
                    orderStatus,
                    paymentCancellationByPaymentId.get(payment.getId())
            );
            splitEntries.add(new SplitEntry(sortKey(payment), detail, payment.getAmount()));
        }

        for (CashPayment cashPayment : cashPayments) {
            SplitPaymentDetailResponse detail = SplitPaymentDetailResponse.fromCash(
                    cashPayment,
                    orderStatus,
                    cashRefundByPaymentId.get(cashPayment.getId())
            );
            splitEntries.add(new SplitEntry(sortKey(cashPayment), detail, cashPayment.getAmount()));
        }

        splitEntries.sort(Comparator.comparing(SplitEntry::sortKey, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<SplitPaymentDetailResponse> splitDetails = splitEntries.stream()
                .map(SplitEntry::detail)
                .toList();

        int totalPaymentAmount = splitEntries.stream()
                .map(SplitEntry::amount)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        SplitPaymentDetailResponse representative = splitDetails.get(0);

        boolean orderMarkedRefunded = orderStatusEnum == OrderStatus.REFUNDED
                || orderStatusEnum == OrderStatus.PARTIALLY_REFUNDED;

        boolean splitMarkedRefunded = splitDetails.stream()
                .anyMatch(split -> split.representativePaymentDetailStatus() == RepresentativePaymentDetailStatus.REFUNDED);

        boolean hasRefundedSplit = orderMarkedRefunded
                || !paymentCancellationByPaymentId.isEmpty()
                || splitMarkedRefunded;

        String representativePaymentStatus = hasRefundedSplit ? "REFUNDED" : "COMPLETED";
        List<OrderItemSummaryResponse> orderItems = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .filter(Objects::nonNull)
                .map(OrderItemSummaryResponse::fromEntity)
                .toList()
                : List.of();

        Integer tableNumber = order.getRestaurantTable() != null
                ? order.getRestaurantTable().getTableNumber()
                : null;

        return new OrderPaymentDetailResponse(
                order.getOrderId(),
                totalPaymentAmount,
                splitDetails.size(),
                representative.paymentMethod(),
                representativePaymentStatus,
                representative.approvedAt(),
                tableNumber,
                orderStatus,
                order.getRemainingAmount(),
                orderItems,
                splitDetails
        );
    }

    private static ZonedDateTime sortKey(Payment payment) {
        if (payment.getApprovedAt() != null) {
            return payment.getApprovedAt().atZoneSameInstant(DEFAULT_ZONE);
        }
        return payment.getCreatedAt().atZoneSameInstant(DEFAULT_ZONE);
    }
    private static ZonedDateTime sortKey(CashPayment cashPayment) {
        if (cashPayment.getReceivedAt() != null) {
            return cashPayment.getReceivedAt().atZoneSameInstant(DEFAULT_ZONE);
        }
        return null;
    }

    private record SplitEntry(
            ZonedDateTime sortKey,
            SplitPaymentDetailResponse detail,
            Integer amount
    ) {
    }
}
