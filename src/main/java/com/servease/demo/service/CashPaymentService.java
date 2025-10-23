package com.servease.demo.service;

import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.repository.CashPaymentRepository;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashPaymentService {

    private final CashPaymentRepository cashPaymentRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentConfirmResponse applyCashPayment(Long orderId, Integer amount) {
        Order order = orderService.getOrderByIdWithLock(orderId);

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "취소된 주문은 결제할 수 없습니다.");
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
            eventPublisher.publishEvent(new OrderFullyPaidEvent(order.getId()));
        }

        return PaymentConfirmResponse.fromCashPayment(cashPayment, order);
    }
}
