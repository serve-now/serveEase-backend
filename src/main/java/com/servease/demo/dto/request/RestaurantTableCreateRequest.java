package com.servease.demo.dto.request;

import com.servease.demo.model.enums.RestaurantTableStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantTableCreateRequest {
    private Integer restaurantTableNumber;

}
