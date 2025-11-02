package com.servease.demo.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.model.enums.PaymentMethodFilter;
import com.servease.demo.model.enums.PaymentQuickRange;
import com.servease.demo.dto.request.PaymentSearchRequest;
import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.dto.response.PaymentDetailResponse;
import com.servease.demo.dto.response.PaymentListResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.infra.TossPaymentClient;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.repository.PaymentRepository;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    // 사용자가 결제창에서 인증 끝내고 successUrl로 돌아온 순간에 paymentKey, orderId, amount를 서버에 저장
    // 같은트랜잭션으로 saveAndConfirm 바로 호출
    // 10분 넘기면 EXPIRED/NOT_FOUND_PAYMENT_SESSION으로 실패 (toss에서)

    //(confirm)
    public PaymentConfirmResponse confirmAndSave(TossConfirmRequest tossConfirmRequest) {
        //트랜잭션 밖에서: 토스 pg에 결제승인 요청
        PaymentResponseDto paymentResponseDto = tossPaymentClient.confirm(tossConfirmRequest);

        //트랜잭션 시작: 재검증 + 내부 DB 반영 + 상태 전이 등
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        PaymentConfirmResponse paymentConfirmResponse = transactionTemplate.execute(status -> processAfterConfirm(tossConfirmRequest, paymentResponseDto));

        if (paymentConfirmResponse == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "결제 처리에 실패했습니다.");
        }

        return paymentConfirmResponse;
    }

    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getPayments(Pageable pageable) {
        return getPayments(pageable, null);
    }

    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getPayments(Pageable pageable, PaymentSearchRequest searchRequest) {
        Specification<Payment> specification = buildSpecification(searchRequest);
        return paymentRepository.findAll(specification, pageable)
                .map(PaymentListResponse::from);
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.findWithOrderById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "결제 내역을 찾을 수 없습니다."));

        PaymentResponseDto paymentResponseDto = deserializePaymentRaw(payment.getRaw());
        return PaymentDetailResponse.of(payment, paymentResponseDto);
    }

    //내부 시스템에 반영 (save직전까지)
    //주문 조회 및 락
    private PaymentConfirmResponse processAfterConfirm(TossConfirmRequest tossConfirmRequest, PaymentResponseDto paymentResponseDto) {
        //멱등,중복 방지
        if (paymentRepository.existsByPaymentKey(tossConfirmRequest.paymentKey())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_KEY, "이미 처리된 결제 요청입니다. (paymentKey)");
        }
        if (paymentRepository.existsByExternalOrderId(tossConfirmRequest.orderId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER_ID, "이미 처리된 결제 요청입니다. (orderId)");
        }

        //상위 주문 락 + 금액 재검증
        Order order = orderService.getOrderByOrderIdWithLock(tossConfirmRequest.parentOrderId());
        order.syncPaymentAmountsWithTotal();

        if (order.isPaid()) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }

        int remainingAmount = order.getRemainingAmount();
        int requestedAmount = tossConfirmRequest.amount();

        if (requestedAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "결제 금액은 0보다 커야합니다.");
        }

        if (requestedAmount > remainingAmount) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_AMOUNT_EXCEEDS_REMAINING,
                    String.format("남은 금액(%d)을 초과하는 금액(%d)으로 결제할 수 없습니다.", remainingAmount, requestedAmount)
            );
        }


        order.recordPayment(requestedAmount);
        orderService.releaseTableIfOrderCompleted(order);

        Payment payment = Payment.from(
                order,
                tossConfirmRequest.paymentKey(),
                tossConfirmRequest.orderId(),
                requestedAmount,
                order.getPaidAmount(),
                paymentResponseDto.getApprovedAt(),
                paymentResponseDto.getMethod(),
                serializeResponse(paymentResponseDto)
        );

        paymentRepository.save(payment);

        if (order.isPaid()) {
            eventPublisher.publishEvent(new OrderFullyPaidEvent(order.getId()));
        }

        return PaymentConfirmResponse.from(paymentResponseDto, order);
    }

    private PaymentResponseDto deserializePaymentRaw(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(rawJson, PaymentResponseDto.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize payment response", e);
            return null;
        }
    }

    //paymentResponseDto 를 문자열로 저장하기위해 json 으로변환
    private String serializeResponse(PaymentResponseDto response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payment response", e);
            return response.toString();
        }
    }


    //동적 쿼리 조합 Specification 이용하여 where 절을 객체로 표현
    //Specification을 조립하는 buildSpecification 과 matchesMethod 로 실행
    private Specification<Payment> buildSpecification(PaymentSearchRequest searchRequest) {
        Specification<Payment> specification = alwaysTrue();

        if (searchRequest == null) {
            return specification;
        }

        DateRange dateRange = calculateSearchDateRange(searchRequest);
        if (dateRange != null) {
            specification = specification.and(matchesDateBetween(dateRange));
        }

        if (searchRequest.paymentMethod() != null) {
            specification = specification.and(matchesMethod(searchRequest.paymentMethod()));
        }

        return specification;
    }

    private Specification<Payment> matchesMethod(PaymentMethodFilter methodFilter) {
        return (root, query, cb) -> {
            var methodPath = root.get("method");
            var notNull = cb.isNotNull(methodPath);
            var upperMethod = cb.upper(methodPath.as(String.class));

            var matches = switch (methodFilter) {
                case CARD -> upperMethod.in("CARD", "EASY_PAY");
                default -> cb.equal(upperMethod, methodFilter.name());
            };

            return cb.and(notNull, matches);
        };
    }

    private Specification<Payment> matchesDateBetween(DateRange dateRange) {
        return (root, query, cb) -> {
            var approvedAtPath = root.<OffsetDateTime>get("approvedAt");
            var createdAtPath = root.<OffsetDateTime>get("createdAt");

            var approvedPredicate = cb.and(
                    cb.isNotNull(approvedAtPath),
                    cb.greaterThanOrEqualTo(approvedAtPath, dateRange.from()),
                    cb.lessThan(approvedAtPath, dateRange.to())
            );

            var createdPredicate = cb.and(
                    cb.isNull(approvedAtPath),
                    cb.greaterThanOrEqualTo(createdAtPath, dateRange.from()),
                    cb.lessThan(createdAtPath, dateRange.to())
            );

            return cb.or(approvedPredicate, createdPredicate);
        };
    }

    private DateRange calculateSearchDateRange(PaymentSearchRequest searchRequest) {
        PaymentQuickRange quickRange = searchRequest.quickRange();
        LocalDate fromDate = searchRequest.from();
        LocalDate toDate = searchRequest.to();

        if (quickRange != null && quickRange != PaymentQuickRange.CUSTOM && (fromDate != null || toDate != null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "단축 기간과 직접 입력 기간은 동시에 사용할 수 없습니다.");
        }

        if ((fromDate == null) != (toDate == null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "기간 검색에는 시작일과 종료일이 모두 필요합니다.");
        }

        if ((quickRange == null || quickRange == PaymentQuickRange.CUSTOM) && fromDate == null) {
            return null;
        }

        if (quickRange == PaymentQuickRange.CUSTOM || quickRange == null) {
            if (fromDate == null || toDate == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "시작일과 종료일을 모두 입력해야 합니다.");
            }
            if (toDate.isBefore(fromDate)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "종료일은 시작일보다 빠를 수 없습니다.");
            }
            return new DateRange(startOfDay(fromDate), startOfDay(toDate.plusDays(1)));
        }

        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        return switch (quickRange) {
            case TODAY -> new DateRange(startOfDay(today), startOfDay(today.plusDays(1)));
            case LAST_7_DAYS -> {
                LocalDate startDate = today.minusDays(6);
                yield new DateRange(startOfDay(startDate), startOfDay(today.plusDays(1)));
            }
            case LAST_30_DAYS -> {
                LocalDate startDate = today.minusDays(29);
                yield new DateRange(startOfDay(startDate), startOfDay(today.plusDays(1)));
            }
            case CUSTOM -> throw new IllegalStateException("CUSTOM 범위는 별도로 처리됩니다.");
        };
    }

    private Specification<Payment> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    private OffsetDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
    }

    private record DateRange(OffsetDateTime from, OffsetDateTime to) {
    }

}
