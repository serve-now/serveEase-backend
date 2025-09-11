package com.servease.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequest {
    private String name;
    private int price;
    private Long categoryId;
    private boolean available;
}
