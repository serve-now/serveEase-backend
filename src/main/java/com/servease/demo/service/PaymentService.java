package com.servease.demo.service;


import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.infra.TossPaymentClient;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;

    // 사용자가 결제창에서 인증 끝내고 successUrl로 돌아온 순간에 paymentKey, orderId, amount를 서버에 저장
    // 같은트랜잭션으로 saveAndConfirm 바로 호출
    // 10분 넘기면 EXPIRED/NOT_FOUND_PAYMENT_SESSION으로 실패 (toss에서)

    public PaymentConfirmResponse confirmAndSave(TossConfirmRequest tossConfirmRequest) {
        //응답에서 orderId 추출,  order 찾기 (completePayment 호출 위함)
        String orderId = tossConfirmRequest.orderId();
        Order order = orderService.getOrderByOrderId(orderId);

        if (!order.getTotalPrice().equals(tossConfirmRequest.amount())) {
            throw new BusinessException(ErrorCode.AMOUNT_NOT_MATCH, "주문 금액과 결제 금액이 일치하지 않습니다.");
        }

        if (order.isPaid()) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }

        PaymentResponseDto response = tossPaymentClient.confirm(tossConfirmRequest);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            //저장 및 order.isPaid= true
            paymentRepository.save(Payment.from(tossConfirmRequest.paymentKey(), orderId, tossConfirmRequest.amount(), response.toString()));
            orderService.completePayment(order.getId());
        });

        // 프론트 응답 DTO 만들기
        return PaymentConfirmResponse.from(response);
    }
}
