package com.servease.demo.service;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.entity.SalesDaily;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.repository.OrderRepository;
import com.servease.demo.repository.SalesDailyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesAggregationService {

    private final OrderRepository orderRepository;
    private final SalesDailyRepository salesDailyRepository;

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

        RestaurantTable restaurantTable = order.getRestaurantTable();
        if (restaurantTable == null || restaurantTable.getStore() == null) {
            log.warn("Skip daily aggregation. Store not found for order. orderId={}", orderId);
            return;
        }

        Store store = restaurantTable.getStore();
        LocalDateTime paidAt = order.getPaidAt();
        if (paidAt == null) {
            log.warn("Paid timestamp missing for completed order. Falling back to orderTime. orderId={}", orderId);
            paidAt = order.getOrderTime();
        }

        ZoneId storeZone = resolveStoreZone(store);
        LocalDate salesDate = paidAt.atZone(storeZone).toLocalDate();

        long dailyNetSales = calculateDailyNetSales(order);

        SalesDaily salesDaily = salesDailyRepository.findByStoreIdAndDate(store.getId(), salesDate)
                .map(existing -> {
                    existing.applyOrder(dailyNetSales, 1);
                    return existing;
                })
                .orElseGet(() -> SalesDaily.createDailyAggregate(store, salesDate, dailyNetSales, 1));

        salesDailyRepository.save(salesDaily);
    }

    private long calculateDailyNetSales(Order order) {
        return Optional.ofNullable(order.getPaidAmount()).map(Integer::longValue).orElse(0L);
    }

    private ZoneId resolveStoreZone(Store store) {
        return ZoneId.of("Asia/Seoul");
    }
}
