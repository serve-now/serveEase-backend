package com.servease.demo.dto.request;

import com.servease.demo.model.enums.OrderItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemStatusUpdateRequest {
    @NotNull(message = "새로운 상태를 입력해주세요")
    private OrderItemStatus newStatus;
}
