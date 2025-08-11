package com.servease.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;

    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
    private int quantity;
}