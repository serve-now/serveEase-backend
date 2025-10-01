package com.servease.demo.dto.response;

import com.servease.demo.model.entity.OrderItem;
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
    private int itemPrice; //이건 개당 가격
    private final int totalItemPrice; //수량이 반영된 총 가격

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
                .totalItemPrice(orderItem.getItemPrice() * orderItem.getQuantity())
                .build();
    }
}

