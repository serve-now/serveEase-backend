package com.servease.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreCreateRequest {

    @NotNull(message = "소유자 ID를 입력해주세요.")
    private Long ownerId;

    @NotBlank(message = "매장 이름을 입력해주세요.")
    @Size(max = 100, message = "매장 이름은 100자 이하로 입력해주세요.")
    private String storeName;
}