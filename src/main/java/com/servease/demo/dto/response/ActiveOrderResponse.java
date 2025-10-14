package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ActiveOrderResponse {
    private Long orderId;
    private Integer totalPrice;
    private Integer paidAmount;
    private Integer outstandingAmount;
    private List<OrderItemSummaryResponse> orderItems;

    public static ActiveOrderResponse fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        return ActiveOrderResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .paidAmount(order.getPaidAmount())
                .outstandingAmount(order.getOutstandingAmount())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemSummaryResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}