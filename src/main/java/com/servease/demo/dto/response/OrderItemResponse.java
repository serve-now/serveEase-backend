package com.servease.demo.dto.response;

import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.enums.OrderItemStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class OrderItemResponse {
    private Long orderItemId;
    private Long menuId;
    private String menuName;
    private int quantity;
    private int itemPrice;
    private OrderItemStatus status;

    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        if (orderItem == null || orderItem.getMenu() == null) {
            return null;
        }

        return OrderItemResponse.builder()
                .orderItemId(orderItem.getId())
                .menuId(orderItem.getMenu().getId())
                .menuName(orderItem.getMenu().getName())
                .quantity(orderItem.getQuantity())
                .itemPrice(orderItem.getItemPrice())
                .status(orderItem.getStatus())
                .build();
    }
}

