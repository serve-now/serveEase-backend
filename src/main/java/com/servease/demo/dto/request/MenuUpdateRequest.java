package com.servease.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequest {
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String name;

    @Positive(message = "가격은 0보다 커야 합니다.")
    private int price;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    private boolean available;
}
