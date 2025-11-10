package com.servease.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servease.demo.dto.PaymentResponseDto;
import com.servease.demo.dto.request.PaymentCancelRequest;
import com.servease.demo.dto.request.PaymentSearchRequest;
import com.servease.demo.dto.request.TossConfirmRequest;
import com.servease.demo.dto.response.OrderPaymentDetailResponse;
import com.servease.demo.dto.response.OrderPaymentListResponse;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.dto.response.PaymentCancelResponse;
import com.servease.demo.dto.response.PaymentListResponse;
import com.servease.demo.dto.request.TossCancelRequest;
import com.servease.demo.dto.response.TossCancelResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.infra.TossPaymentClient;
import com.servease.demo.model.entity.CashPayment;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.Payment;
import com.servease.demo.model.entity.PaymentCancellation;
import com.servease.demo.model.entity.CashPaymentRefund;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.model.enums.PaymentMethodFilter;
import com.servease.demo.model.enums.PaymentOrderTypeFilter;
import com.servease.demo.model.enums.PaymentQuickRange;
import com.servease.demo.repository.CashPaymentRepository;
import com.servease.demo.repository.CashPaymentRefundRepository;
import com.servease.demo.repository.PaymentCancellationRepository;
import com.servease.demo.repository.PaymentRepository;
import com.servease.demo.service.event.OrderFullyPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.criteria.JoinType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.servease.demo.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final CashPaymentRepository cashPaymentRepository;
    private final CashPaymentRefundRepository cashPaymentRefundRepository;
    private final PaymentCancellationRepository paymentCancellationRepository;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationEventPublisher eventPublisher;
    private final SettlementService settlementService;
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

    public PaymentCancelResponse cancel(PaymentCancelRequest request) {
        Payment payment = preValidateCancel(request);
        TossCancelContext cancelContext = requestTossCancel(payment.getPaymentKey(), request.cancelAmount());
        return processAfterCancel(payment.getPaymentKey(), cancelContext);
    }

    @Transactional(readOnly = true)
    public Page<OrderPaymentListResponse> getPayments(Pageable pageable, PaymentSearchRequest searchRequest) {
        if (searchRequest == null || searchRequest.storeId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "storeId가 필요합니다.");
        }

        Specification<Payment> specification = buildSpecification(searchRequest);
        Sort sort = resolveSort(pageable);

        List<Payment> payments = paymentRepository.findAll(specification, sort);

        Map<String, List<Payment>> groupedByOrderId = new LinkedHashMap<>();
        Set<String> orderedByPayment = new LinkedHashSet<>();
        for (Payment payment : payments) {
            Order order = payment.getOrder();
            if (order == null) {
                log.warn("Skipping payment {} because associated order is null", payment.getId());
                continue;
            }

            groupedByOrderId.computeIfAbsent(order.getOrderId(), id -> new LinkedList<>())
                    .add(payment);
            orderedByPayment.add(order.getOrderId());
        }

        Map<String, List<CashPayment>> cashPaymentsByOrderId = new LinkedHashMap<>();
        if (shouldIncludeCashPayments(searchRequest)) {
            Specification<CashPayment> cashSpecification = buildCashSpecification(searchRequest);
            Sort cashSort = Sort.by(Sort.Direction.DESC, "receivedAt", "id");
            List<CashPayment> cashPayments = cashPaymentRepository.findAll(cashSpecification, cashSort);
            for (CashPayment cashPayment : cashPayments) {
                Order order = cashPayment.getOrder();
                if (order == null) {
                    log.warn("Skipping cash payment {} because associated order is null", cashPayment.getId());
                    continue;
                }
                cashPaymentsByOrderId.computeIfAbsent(order.getOrderId(), id -> new LinkedList<>())
                        .add(cashPayment);
            }
        }

        LinkedHashSet<String> allOrderIds = new LinkedHashSet<>(orderedByPayment);
        for (String cashOrderId : cashPaymentsByOrderId.keySet()) {
            if (!allOrderIds.contains(cashOrderId)) {
                allOrderIds.add(cashOrderId);
            }
        }

        List<OrderPaymentListResponse> summaries = new ArrayList<>(allOrderIds.size());
        for (String orderId : allOrderIds) {
            List<Payment> paymentGroup = groupedByOrderId.getOrDefault(orderId, List.of());
            List<CashPayment> cashGroup = cashPaymentsByOrderId.getOrDefault(orderId, List.of());

            Order order = null;
            if (!paymentGroup.isEmpty()) {
                order = paymentGroup.get(0).getOrder();
            } else if (!cashGroup.isEmpty()) {
                order = cashGroup.get(0).getOrder();
            }

            if (order == null) {
                log.warn("Skipping order {} because associated order is null in payment summary aggregation", orderId);
                continue;
            }

            summaries.add(OrderPaymentListResponse.from(order, paymentGroup, cashGroup));
        }

        summaries.sort(
                Comparator.comparing(
                                OrderPaymentListResponse::representativeApprovedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .reversed()
        );

        int total = summaries.size();
        int offset = calculateOffset(pageable, total);
        int toIndex = Math.min(offset + pageable.getPageSize(), total);

        List<OrderPaymentListResponse> pageContent = offset >= total
                ? List.of()
                : summaries.subList(offset, toIndex);

        return new PageImpl<>(pageContent, pageable, total);
    }

    @Transactional(readOnly = true)
    public OrderPaymentDetailResponse getPaymentDetailByOrderId(String orderId) {
        Order order = orderService.getOrderByOrderId(orderId);
        validateOrderOwnership(order);

        List<Payment> payments = paymentRepository.findByOrderOrderId(orderId);
        Map<Long, PaymentResponseDto> responseByPaymentId = new LinkedHashMap<>();
        for (Payment each : payments) {
            responseByPaymentId.put(each.getId(), deserializePaymentRaw(each.getRaw()));
        }

        List<PaymentResponseDto> paymentResponses = payments.stream()
                .map(each -> responseByPaymentId.get(each.getId()))
                .toList();

        Map<Long, PaymentCancellation> cancellationsByPaymentId = payments.isEmpty()
                ? Map.of()
                : paymentCancellationRepository.findByPaymentIdIn(
                        payments.stream()
                                .map(Payment::getId)
                                .toList()
                ).stream()
                .collect(Collectors.toMap(
                        cancellation -> cancellation.getPayment().getId(),
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));

        List<CashPayment> cashPayments = cashPaymentRepository.findByOrderOrderId(orderId);

        Map<Long, CashPaymentRefund> cashRefundsByPaymentId = cashPayments.isEmpty()
                ? Map.of()
                : cashPaymentRefundRepository.findByCashPaymentIdIn(
                        cashPayments.stream()
                                .map(CashPayment::getId)
                                .toList()
                ).stream()
                .collect(Collectors.toMap(
                        refund -> refund.getCashPayment().getId(),
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));

        if (payments.isEmpty() && cashPayments.isEmpty()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "결제 내역을 찾을 수 없습니다.");
        }

        return OrderPaymentDetailResponse.from(
                order,
                payments,
                paymentResponses,
                cashPayments,
                cancellationsByPaymentId,
                cashRefundsByPaymentId
        );
    }

    private void validateOrderOwnership(Order order) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        var table = order.getRestaurantTable();
        if (table == null || table.getStore() == null || table.getStore().getOwner() == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "주문에 연결된 가게 정보를 찾을 수 없습니다.");
        }

        Long ownerId = table.getStore().getOwner().getId();
        if (!Objects.equals(ownerId, user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "다른 가게의 결제 내역에는 접근할 수 없습니다.");
        }
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
            Long orderId = order.getId();
            String paymentKey = payment.getPaymentKey();
            Integer paidAmount = order.getPaidAmount();

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("[PAYMENT] Order fully paid. publishing settlement event orderId={}, paymentKey={}, paidAmount={}",
                                orderId, paymentKey, paidAmount);
                        eventPublisher.publishEvent(new OrderFullyPaidEvent(orderId));
                    }
                });
            } else {
                log.warn("[PAYMENT] Transaction synchronization inactive. publishing settlement event immediately orderId={}",
                        orderId);
                eventPublisher.publishEvent(new OrderFullyPaidEvent(orderId));
            }
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

    private String serializeCancelResponse(TossCancelResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize toss cancel response", e);
            return response.toString();
        }
    }

    private Payment preValidateCancel(PaymentCancelRequest request) {
        Payment payment = paymentRepository.findByPaymentKey(request.paymentKey())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "취소할 결제 정보를 찾을 수 없습니다."));

        if (paymentCancellationRepository.existsByPaymentId(payment.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELED, "해당 결제는 이미 취소되었습니다.");
        }

        if (!Objects.equals(payment.getAmount(), request.cancelAmount())) {
            throw new BusinessException(
                    ErrorCode.CANCEL_AMOUNT_NOT_MATCH,
                    String.format("요청한 취소 금액(%d)이 결제 금액(%d)과 일치하지 않습니다.",
                            request.cancelAmount(), payment.getAmount()));
        }

        return payment;
    }

    private TossCancelContext requestTossCancel(String paymentKey, Integer cancelAmount) {
        TossCancelRequest tossCancelRequest = new TossCancelRequest(cancelAmount);
        TossCancelResponse response = tossPaymentClient.cancel(paymentKey, tossCancelRequest);

        TossCancelResponse.CancelHistory latest = extractLatestCancelHistory(response);
        OffsetDateTime canceledAt = latest != null && latest.getCanceledAt() != null
                ? latest.getCanceledAt()
                : OffsetDateTime.now();
        String cancelReason = latest != null && latest.getCancelReason() != null && !latest.getCancelReason().isBlank()
                ? latest.getCancelReason()
                : tossCancelRequest.cancelReason();

        return new TossCancelContext(response, canceledAt, cancelReason);
    }

    private PaymentCancelResponse processAfterCancel(String paymentKey, TossCancelContext context) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        PaymentCancelResponse response = transactionTemplate.execute(status -> {
            Payment paymentForUpdate = paymentRepository.findByPaymentKey(paymentKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "취소할 결제 정보를 찾을 수 없습니다."));

            if (paymentCancellationRepository.existsByPaymentId(paymentForUpdate.getId())) {
                throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELED, "해당 결제는 이미 취소되었습니다.");
            }

            Order order = orderService.getOrderByIdWithLock(paymentForUpdate.getOrder().getId());

            order.refundPayment(paymentForUpdate.getAmount());

            PaymentCancellation cancellation = PaymentCancellation.of(
                    paymentForUpdate,
                    paymentForUpdate.getAmount(),
                    context.cancelReason(),
                    context.canceledAt(),
                    serializeCancelResponse(context.response())
            );
            paymentCancellationRepository.save(cancellation);

            PaymentCancelResponse cancelResponse = PaymentCancelResponse.from(
                    paymentForUpdate,
                    order,
                    paymentForUpdate.getAmount(),
                    context.cancelReason(),
                    context.canceledAt()
            );

            scheduleRefundSettlement(order, paymentForUpdate.getAmount(), context.canceledAt());

            return cancelResponse;
        });

        if (response == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "결제 취소 처리에 실패했습니다.");
        }

        return response;
    }

    private record TossCancelContext(
            TossCancelResponse response,
            OffsetDateTime canceledAt,
            String cancelReason
    ) {}

    private TossCancelResponse.CancelHistory extractLatestCancelHistory(TossCancelResponse response) {
        if (response.getCancels() == null || response.getCancels().isEmpty()) {
            return null;
        }
        return response.getCancels().get(response.getCancels().size() - 1);
    }

    private void scheduleRefundSettlement(Order order, Integer cancelAmount, OffsetDateTime canceledAt) {
        Long storeId = resolveStoreId(order);
        if (storeId == null) {
            log.warn("[REFUND] store not found for order {}, skip settlement aggregation", order.getId());
            return;
        }

        Long orderId = order.getId();
        Runnable record = () -> {
            log.info("[REFUND] card refund processed. aggregating settlement orderId={}, storeId={}, amount={}",
                    orderId, storeId, cancelAmount);
            settlementService.recordRefund(orderId, storeId, cancelAmount, canceledAt);
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    record.run();
                }
            });
        } else {
            record.run();
        }
    }

    private Long resolveStoreId(Order order) {
        if (order.getStore() != null) {
            return order.getStore().getId();
        }

        RestaurantTable table = order.getRestaurantTable();
        if (table != null && table.getStore() != null) {
            return table.getStore().getId();
        }

        return null;
    }


    //동적 쿼리 조합 Specification 이용하여 where 절을 객체로 표현
    //Specification을 조립하는 buildSpecification 과 matchesMethod 로 실행
    private Specification<Payment> buildSpecification(PaymentSearchRequest searchRequest) {
        Specification<Payment> specification = alwaysTrue();

        if (searchRequest == null) {
            return specification;
        }

        if (searchRequest.storeId() != null) {
            specification = specification.and(matchesStore(searchRequest.storeId()));
        }

        DateRange dateRange = calculateSearchDateRange(searchRequest);
        if (dateRange != null) {
            specification = specification.and(matchesDateBetween(dateRange));
        }

        if (searchRequest.paymentMethod() != null) {
            specification = specification.and(matchesMethod(searchRequest.paymentMethod()));
        }

        if (searchRequest.orderType() != null) {
            specification = specification.and(matchesOrderType(searchRequest.orderType()));
        }

        return specification;
    }

    private Specification<Payment> matchesMethod(PaymentMethodFilter methodFilter) {
        return (root, query, cb) -> {
            var methodPath = root.get("method");
            var notNull = cb.isNotNull(methodPath);
            var upperMethod = cb.upper(methodPath.as(String.class));

            Set<String> acceptedMethods = methodFilter.acceptedMethods().stream()
                    .map(method -> method.toUpperCase(Locale.ROOT))
                    .collect(Collectors.toSet());

            if (acceptedMethods.isEmpty()) {
                return cb.disjunction();
            }

            return cb.and(notNull, upperMethod.in(acceptedMethods));
        };
    }

    private Specification<Payment> matchesOrderType(PaymentOrderTypeFilter orderTypeFilter) {
        return (root, query, cb) -> {
            var orderJoin = root.join("order");
            var statusPath = orderJoin.get("status");
            var paidTotalPath = root.get("paidTotalAfterPayment").as(Integer.class);
            var amountPath = root.get("amount").as(Integer.class);

            return switch (orderTypeFilter) {
                case CANCELED -> cb.equal(statusPath, OrderStatus.CANCELED);
                case REFUNDED -> statusPath.in(OrderStatus.REFUNDED, OrderStatus.PARTIALLY_REFUNDED);
                case PARTIAL -> cb.or(
                        cb.equal(statusPath, OrderStatus.PARTIALLY_PAID),
                        cb.and(
                                cb.equal(statusPath, OrderStatus.COMPLETED),
                                cb.greaterThan(paidTotalPath, amountPath)
                        )
                );
                case NORMAL -> cb.and(
                        cb.or(
                                cb.isNull(statusPath),
                                cb.notEqual(statusPath, OrderStatus.CANCELED)
                        ),
                        cb.lessThanOrEqualTo(paidTotalPath, amountPath)
                );
            };
        };
    }

    private Specification<Payment> matchesStore(Long storeId) {
        return (root, query, cb) -> {
            var orderJoin = root.join("order");
            var tableJoin = orderJoin.join("restaurantTable", JoinType.LEFT);
            var storeJoin = tableJoin.join("store", JoinType.LEFT);
            return cb.equal(storeJoin.get("id"), storeId);
        };
    }

    private Specification<CashPayment> buildCashSpecification(PaymentSearchRequest searchRequest) {
        Specification<CashPayment> specification = alwaysTrueCash();

        if (searchRequest == null) {
            return specification;
        }

        if (searchRequest.storeId() != null) {
            specification = specification.and(matchesCashStore(searchRequest.storeId()));
        }

        DateRange dateRange = calculateSearchDateRange(searchRequest);
        if (dateRange != null) {
            specification = specification.and(matchesCashDateBetween(dateRange));
        }

        if (searchRequest.orderType() != null) {
            specification = specification.and(matchesCashOrderType(searchRequest.orderType()));
        }

        return specification;
    }

    private Specification<CashPayment> matchesCashDateBetween(DateRange dateRange) {
        return (root, query, cb) -> {
            var receivedAt = root.<OffsetDateTime>get("receivedAt");
            return cb.and(
                    cb.greaterThanOrEqualTo(receivedAt, dateRange.from()),
                    cb.lessThan(receivedAt, dateRange.to())
            );
        };
    }

    private Specification<CashPayment> matchesCashOrderType(PaymentOrderTypeFilter orderTypeFilter) {
        return (root, query, cb) -> {
            var orderJoin = root.join("order");
            var statusPath = orderJoin.get("status");
            var paidTotalPath = root.get("paidTotalAfterPayment").as(Integer.class);
            var amountPath = root.get("amount").as(Integer.class);

            return switch (orderTypeFilter) {
                case CANCELED -> cb.equal(statusPath, OrderStatus.CANCELED);
                case REFUNDED -> statusPath.in(OrderStatus.REFUNDED, OrderStatus.PARTIALLY_REFUNDED);
                case PARTIAL -> cb.or(
                        cb.equal(statusPath, OrderStatus.PARTIALLY_PAID),
                        cb.and(
                                cb.equal(statusPath, OrderStatus.COMPLETED),
                                cb.greaterThan(paidTotalPath, amountPath)
                        )
                );
                case NORMAL -> cb.and(
                        cb.or(
                                cb.isNull(statusPath),
                                cb.notEqual(statusPath, OrderStatus.CANCELED)
                        ),
                        cb.lessThanOrEqualTo(paidTotalPath, amountPath)
                );
            };
        };
    }

    private Specification<CashPayment> matchesCashStore(Long storeId) {
        return (root, query, cb) -> {
            var orderJoin = root.join("order");
            var tableJoin = orderJoin.join("restaurantTable", JoinType.LEFT);
            var storeJoin = tableJoin.join("store", JoinType.LEFT);
            return cb.equal(storeJoin.get("id"), storeId);
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

    private Specification<CashPayment> alwaysTrueCash() {
        return (root, query, cb) -> cb.conjunction();
    }

    private Specification<CashPayment> alwaysFalseCash() {
        return (root, query, cb) -> cb.disjunction();
    }

    private boolean shouldIncludeCashPayments(PaymentSearchRequest searchRequest) {
        if (searchRequest == null || searchRequest.paymentMethod() == null) {
            return true;
        }

        return searchRequest.paymentMethod().acceptedMethods().stream()
                .anyMatch(method -> "CASH".equalsIgnoreCase(method));
    }

    private Comparator<PaymentListResponse> buildComparator(Sort sort) {
        Comparator<PaymentListResponse> comparator = null;

        if (sort != null && sort.isSorted()) {
            for (Sort.Order order : sort) {
                Comparator<PaymentListResponse> propertyComparator = comparatorForOrder(order);
                if (propertyComparator != null) {
                    comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
                }
            }
        }

        if (comparator == null) {
            comparator = defaultComparator();
        }

        return comparator;
    }

    private Comparator<PaymentListResponse> defaultComparator() {
        Comparator<PaymentListResponse> primary = comparing(
                PaymentListResponse::approvedAt,
                Comparator.naturalOrder(),
                true
        );

        Comparator<PaymentListResponse> secondary = comparing(
                PaymentListResponse::paymentId,
                Comparator.naturalOrder(),
                true
        );

        return primary.thenComparing(secondary);
    }

    private Comparator<PaymentListResponse> comparatorForOrder(Sort.Order order) {
        String property = order.getProperty();
        boolean descending = order.isDescending();

        return switch (property) {
            case "paymentId" -> comparing(PaymentListResponse::paymentId, Comparator.naturalOrder(), descending);
            case "orderId" -> comparing(PaymentListResponse::orderId, String.CASE_INSENSITIVE_ORDER, descending);
            case "paymentMethod" -> comparing(PaymentListResponse::paymentMethod, String.CASE_INSENSITIVE_ORDER, descending);
            case "approvedAt" -> comparing(PaymentListResponse::approvedAt, Comparator.naturalOrder(), descending);
            case "totalPaymentAmount" -> comparing(PaymentListResponse::totalPaymentAmount, Comparator.naturalOrder(), descending);
            case "paymentStatus" -> comparing(PaymentListResponse::paymentStatus, String.CASE_INSENSITIVE_ORDER, descending);
            case "representativeItemName" -> comparing(PaymentListResponse::representativeItemName, String.CASE_INSENSITIVE_ORDER, descending);
            case "totalItemCount" -> comparing(PaymentListResponse::totalItemCount, Comparator.naturalOrder(), descending);
            default -> null;
        };
    }

    private <T> Comparator<PaymentListResponse> comparing(
            Function<PaymentListResponse, T> extractor,
            Comparator<T> valueComparator,
            boolean descending
    ) {
        Comparator<PaymentListResponse> comparator = Comparator.comparing(
                extractor,
                Comparator.nullsLast(valueComparator)
        );

        if (descending) {
            return (left, right) -> comparator.compare(right, left);
        }

        return comparator;
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

    private Sort resolveSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable.getSort();
        }
        return Sort.by(Sort.Direction.DESC, "approvedAt", "createdAt");
    }

    private int calculateOffset(Pageable pageable, int totalElements) {
        long rawOffset = pageable.getOffset();
        if (rawOffset >= totalElements) {
            return totalElements;
        }
        if (rawOffset > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) rawOffset;
    }

    private record DateRange(OffsetDateTime from, OffsetDateTime to) {
    }

}
