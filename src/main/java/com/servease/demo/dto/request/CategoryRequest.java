package com.servease.demo.dto.request;

import com.servease.demo.model.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequest {
    private String name;

    public Category toEntity() {
        return Category.builder()
                .name(this.name)
                .build();
    }
}