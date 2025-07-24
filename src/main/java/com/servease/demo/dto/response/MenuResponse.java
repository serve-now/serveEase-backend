package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Menu;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Long id;
    private String name;
    private Integer price;
    private String category;
    private Boolean isAvailable;

    //클래스 인스턴스를 생성하지 않고 호출할 수 있게 static 으로 선언
    public static MenuResponse fromEntity(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .category(menu.getCategory())
                .isAvailable(menu.getIsAvailable())
                .build();
    }
}
