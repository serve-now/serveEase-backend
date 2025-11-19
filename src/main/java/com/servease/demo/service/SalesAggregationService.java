package com.servease.demo.service;

import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.entity.SalesDaily;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.repository.OrderRepository;
import com.servease.demo.repository.SalesDailyRepository;
import com.servease.demo.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesAggregationService {

    private final OrderRepository orderRepository;
    private final SalesDailyRepository salesDailyRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void upsertDaily(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            log.warn("Skip daily aggregation. Order not found. orderId={}", orderId);
            return;
        }

        Order order = orderOptional.get();
        if (order.getStatus() != OrderStatus.COMPLETED || !order.isPaid()) {
            log.debug("Skip daily aggregation. Order not completed. orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        Store store = resolveStore(order);
        if (store == null) {
            log.warn("Skip daily aggregation. Store not found for order. orderId={}", orderId);
            return;
        }

        ZoneId storeZone = resolveStoreZone(store);
        OffsetDateTime paidAt = resolvePaidAt(order, storeZone);
        LocalDate salesDate = paidAt.atZoneSameInstant(storeZone).toLocalDate();

        long dailyNetSales = calculateDailyNetSales(order);

        SalesDaily salesDaily = salesDailyRepository.findByStoreIdAndDate(store.getId(), salesDate)
                .map(existing -> {
                    existing.applyOrder(dailyNetSales, 1);
                    return existing;
                })
                .orElseGet(() -> SalesDaily.createDailyAggregate(store, salesDate, dailyNetSales, 1));

        salesDailyRepository.save(salesDaily);
    }

    @Transactional
    public void recordRefund(Long storeId, OffsetDateTime refundedAt, Integer refundAmount) {
        if (storeId == null || refundAmount == null || refundAmount <= 0) {
            log.warn("Skip daily refund aggregation. Invalid storeId/refundAmount storeId={}, refundAmount={}",
                    storeId, refundAmount);
            return;
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "정산용 스토어를 찾을 수 없습니다. storeId=" + storeId));

        ZoneId storeZone = resolveStoreZone(store);
        OffsetDateTime effectiveRefundedAt = refundedAt != null ? refundedAt : OffsetDateTime.now(storeZone);
        LocalDate refundDate = effectiveRefundedAt.atZoneSameInstant(storeZone).toLocalDate();

        SalesDaily salesDaily = salesDailyRepository.findByStoreIdAndDate(storeId, refundDate)
                .orElseGet(() -> SalesDaily.createDailyAggregate(store, refundDate, 0L, 0));

        long refundDelta = refundAmount.longValue();
        long currentNet = Optional.ofNullable(salesDaily.getDailyNetSales()).orElse(0L);
        long currentCanceled = Optional.ofNullable(salesDaily.getDailyCanceledAmount()).orElse(0L);
        int currentOrderCount = Optional.ofNullable(salesDaily.getOrderCount()).orElse(0);

        salesDaily.setDailyNetSales(currentNet - refundDelta);
        salesDaily.setDailyCanceledAmount(currentCanceled + refundDelta);
        salesDaily.setOrderCount(currentOrderCount);

        if (currentOrderCount <= 0) {
            salesDaily.setDailyAverageOrderValue(BigDecimal.ZERO);
        } else {
            salesDaily.setDailyAverageOrderValue(BigDecimal.valueOf(salesDaily.getDailyNetSales())
                    .divide(BigDecimal.valueOf(currentOrderCount), 2, RoundingMode.HALF_UP));
        }

        salesDailyRepository.save(salesDaily);
    }

    private long calculateDailyNetSales(Order order) {
        return Optional.ofNullable(order.getPaidAmount()).map(Integer::longValue).orElse(0L);
    }

    private OffsetDateTime resolvePaidAt(Order order, ZoneId storeZone) {
        OffsetDateTime paidAt = order.getPaidAt();
        if (paidAt != null) {
            return paidAt;
        }

        log.warn("Paid timestamp missing for completed order. Falling back to orderTime. orderId={}", order.getId());
        LocalDateTime orderTime = order.getOrderTime();
        if (orderTime == null) {
            return OffsetDateTime.now(storeZone);
        }
        return orderTime.atZone(storeZone).toOffsetDateTime();
    }

    private ZoneId resolveStoreZone(Store store) {
        return ZoneId.of("Asia/Seoul");
    }

    private Store resolveStore(Order order) {
        if (order.getStore() != null) {
            return order.getStore();
        }
        RestaurantTable table = order.getRestaurantTable();
        return table != null ? table.getStore() : null;
    }
}
