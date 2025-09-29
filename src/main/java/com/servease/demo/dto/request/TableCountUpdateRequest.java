package com.servease.demo.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TableCountUpdateRequest {

    @Min(value = 1, message = "테이블 수는 1 이상이어야 합니다.")
    private int newTotalCount;
}