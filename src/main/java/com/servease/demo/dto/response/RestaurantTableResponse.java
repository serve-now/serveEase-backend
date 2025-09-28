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
            status = activeOrderOpt.get().getStatus().name();// "ORDERED" or "SERVED"
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
}
