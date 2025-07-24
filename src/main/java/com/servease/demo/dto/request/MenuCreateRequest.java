package com.servease.demo.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequest {
    //id 는 자동생성
    private String name;
    private int price;
    private String category;
    private boolean isAvailable;
}
