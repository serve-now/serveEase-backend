package com.servease.demo.dto.response;

import com.servease.demo.model.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemSummaryResponse {
    private String menuName;
    private int quantity;
    private long price;

    public static OrderItemSummaryResponse fromEntity(OrderItem orderItem) {
        return OrderItemSummaryResponse.builder()
                .menuName(orderItem.getMenu().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getItemPrice())
                .build();
    }
}
