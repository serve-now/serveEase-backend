package com.servease.demo.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemQuantityUpdateRequest {
    @Min(value = 0, message = "수량은 0 이상이어야 합니다.")
    private int newQuantity;
}
