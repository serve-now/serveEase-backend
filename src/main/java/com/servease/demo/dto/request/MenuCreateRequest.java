package com.servease.demo.dto.request;

import com.servease.demo.model.entity.Menu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequest {
    //id 는 자동생성
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String name;

    @Positive(message = "가격은 0보다 커야합니다.")
    private int price;

    @NotNull(message = "카테고리 ID 는 필수입니다.")
    private Long categoryId;

    private boolean available = true;

    public Menu toEntity() {
        return Menu.builder()
                .name(this.name)
                .price(this.price)
                .available(this.available)
                .build();
    }
}
