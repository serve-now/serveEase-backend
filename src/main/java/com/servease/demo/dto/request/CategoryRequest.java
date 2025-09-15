package com.servease.demo.dto.request;

import com.servease.demo.model.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "카테고리 이름은 필수입니다.") // 규칙 추가
    private String name;

    public Category toEntity() {
        return Category.builder()
                .name(this.name)
                .build();
    }
}