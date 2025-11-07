package com.servease.demo.service;

import com.servease.demo.dto.request.CashPaymentRefundRequest;
import com.servease.demo.dto.response.CashPaymentRefundResponse;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.CashPaymentRefund;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.repository.CashPaymentRefundRepository;
import com.servease.demo.repository.CashPaymentRepository;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashPaymentService {

    private final CashPaymentRepository cashPaymentRepository;
    private final CashPaymentRefundRepository cashPaymentRefundRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentConfirmResponse applyCashPayment(Long orderId, Integer amount) {
        Order order = orderService.getOrderByIdWithLock(orderId);

        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "취소되었거나 환불된 주문은 결제할 수 없습니다.");
        }

        order.syncPaymentAmountsWithTotal();

        return completeCashPayment(order, amount);
    }


    //내부 호출
    private PaymentConfirmResponse completeCashPayment(Order order, Integer amount) {
        boolean orderCompleted = order.recordPayment(amount);
        orderService.releaseTableIfOrderCompleted(order);

        CashPayment cashPayment = cashPaymentRepository.save(
                CashPayment.of(order, amount, order.getPaidAmount())
        );

        if (orderCompleted) {
            Long orderId = order.getId();
            Integer paidAmount = order.getPaidAmount();

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("[PAYMENT] Order fully paid by cash. publishing settlement event orderId={}, totalPaid={}",
                                orderId, paidAmount);
                        eventPublisher.publishEvent(new OrderFullyPaidEvent(orderId));
                    }
                });
            } else {
                log.warn("[PAYMENT] Transaction synchronization inactive. publishing cash settlement event immediately orderId={}",
                        orderId);
                eventPublisher.publishEvent(new OrderFullyPaidEvent(orderId));
            }
        }

        return PaymentConfirmResponse.fromCashPayment(cashPayment, order);
    }


    @Transactional
    public PaymentConfirmResponse completeOutstandingPayment(Long orderId) {
        Order order = orderService.getOrderByIdWithLock(orderId);

        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "취소되었거나 환불된 주문은 결제할 수 없습니다.");
        }

        order.syncPaymentAmountsWithTotal();

        int remainingAmount = order.getRemainingAmount();
        if (remainingAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "남은 결제 금액이 없습니다.");
        }

        return completeCashPayment(order, remainingAmount);
    }

    @Transactional
    public CashPaymentRefundResponse refundCashPayment(Long orderId, CashPaymentRefundRequest request) {
        Order order = orderService.getOrderByIdWithLock(orderId);

        CashPayment cashPayment = cashPaymentRepository.findById(request.cashPaymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "현금 결제 내역을 찾을 수 없습니다."));

        if (!Objects.equals(cashPayment.getOrder().getId(), order.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 주문의 현금 결제가 아닙니다.");
        }

        if (cashPaymentRefundRepository.existsByCashPaymentId(cashPayment.getId())) {
            throw new BusinessException(ErrorCode.CASH_PAYMENT_ALREADY_REFUNDED, "이미 환불된 현금 결제입니다.");
        }

        Integer refundAmount = request.refundAmount();
        if (!Objects.equals(cashPayment.getAmount(), refundAmount)) {
            throw new BusinessException(
                    ErrorCode.CANCEL_AMOUNT_NOT_MATCH,
                    String.format("요청한 환불 금액(%d)이 현금 결제 금액(%d)과 일치하지 않습니다.",
                            refundAmount, cashPayment.getAmount())
            );
        }

        if (order.getPaidAmount() == null || order.getPaidAmount() < refundAmount) {
            throw new BusinessException(
                    ErrorCode.CANCEL_AMOUNT_EXCEEDS_PAID,
                    String.format("환불 금액(%d)이 주문의 결제 금액(%d)을 초과했습니다.", refundAmount, order.getPaidAmount())
            );
        }

        order.refundPayment(refundAmount);

        CashPaymentRefund refund = cashPaymentRefundRepository.save(
                CashPaymentRefund.of(
                        cashPayment,
                        refundAmount,
                        request.refundReason(),
                        OffsetDateTime.now()
                )
        );

        return CashPaymentRefundResponse.from(order, cashPayment, refund);
    }
}
