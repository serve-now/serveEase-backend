package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OrderResponse {
    private Long id;
    private String orderId;
    private Long restaurantTableId;
    private OrderStatus status;
    private int totalPrice;
    private boolean isPaid;
    private LocalDateTime orderTime;
    private List<OrderItemResponse> orderItems;

    public static OrderResponse fromEntity(Order order) {
        if (order == null || order.getRestaurantTable() == null) {
            return null;
        }

        List<OrderItemResponse> itemResponses = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList())
                : List.of();

        return OrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .restaurantTableId(order.getRestaurantTable().getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .isPaid(order.isPaid())
                .orderTime(order.getOrderTime())
                .orderItems(itemResponses)
                .build();
    }
}
