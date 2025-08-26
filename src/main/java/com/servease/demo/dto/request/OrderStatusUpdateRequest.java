package com.servease.demo.dto.request;

import com.servease.demo.model.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Order의 상태를 변경할 때와 OrderItem의 상태를 변경할 때 모두 재사용할 수 있으나 추후 명확성을 위해 OrderItemDTO 를 추가

@Getter
@NoArgsConstructor
public class OrderStatusUpdateRequest {
    private OrderStatus status;
}