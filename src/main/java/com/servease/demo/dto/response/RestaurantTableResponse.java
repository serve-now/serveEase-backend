package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.OrderStatus;
import lombok.*;

import java.util.Comparator;
import java.util.Optional;

@Setter
@Getter
@Builder
public class RestaurantTableResponse {
    private Long id;
    private Integer restaurantTableNumber;
    private String displayStatus;  // EMPTY / ORDERED / SERVED
    private ActiveOrderResponse activeOrder;

    public static RestaurantTableResponse fromEntity(RestaurantTable restaurantTable){
        if (restaurantTable == null) {
            return null;
        }
        Optional<Order> activeOrderOpt = restaurantTable.getOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.ORDERED || order.getStatus() == OrderStatus.SERVED)
                .max(Comparator.comparing(Order::getOrderTime));

        String status;
        ActiveOrderResponse activeOrderResponse = null;

        if (activeOrderOpt.isPresent()) {
            Order activeOrder = activeOrderOpt.get();
            status = resolveDisplayStatus(activeOrder);
            activeOrderResponse = ActiveOrderResponse.fromEntity(activeOrder);
        } else {
            status = "EMPTY";
        }

        return RestaurantTableResponse.builder()
                .id(restaurantTable.getId())
                .restaurantTableNumber(restaurantTable.getTableNumber())
                .displayStatus(status)
                .activeOrder(activeOrderResponse)
                .build();
    }



    public static RestaurantTableResponse from(RestaurantTable table, Order latestActiveOrder) {
        if (table == null) {
            return null;
        }

        String status;
        ActiveOrderResponse activeOrderResponse = null;

        if (latestActiveOrder != null) {
            status = resolveDisplayStatus(latestActiveOrder); // "ORDERED" or "SERVED"
            activeOrderResponse = ActiveOrderResponse.fromEntity(latestActiveOrder);
        } else {
            status = "EMPTY";
        }

        return RestaurantTableResponse.builder()
                .id(table.getId())
                .restaurantTableNumber(table.getTableNumber())
                .displayStatus(status)
                .activeOrder(activeOrderResponse)
                .build();
    }

    private static String resolveDisplayStatus(Order order) {
        if (order == null) {
            return "EMPTY";
        }

        if (order.isPaid()) {
            return order.getStatus().name();
        }

        if (order.getPaidAmount() != null && order.getPaidAmount() > 0) {
            return "PARTIALLY_PAID";
        }

        return order.getStatus().name();
    }
}
