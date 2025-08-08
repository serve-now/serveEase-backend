package com.servease.demo.dto.response;

import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.RestaurantTableStatus;
import lombok.*;

@Setter
@Getter
@Builder
public class RestaurantTableResponse {
    private Long id;
    private Integer restaurantTableNumber;
    private RestaurantTableStatus status;

    public static RestaurantTableResponse fromEntity(RestaurantTable restaurantTable){
        if(restaurantTable == null) {
            return null;
        }

        return RestaurantTableResponse.builder()
                .id(restaurantTable.getId())
                .restaurantTableNumber(restaurantTable.getTableNumber())
                .status(restaurantTable.getStatus())
                .build();
    }

}
