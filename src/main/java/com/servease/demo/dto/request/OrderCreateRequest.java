package com.servease.demo.dto.request;

import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    private Integer restaurantTableNumber;
    private OrderStatus status;
    private Integer totalPrice;
    private boolean isPaid;
    private LocalDateTime orderTime;
    private List<OrderItem> orderItems = new ArrayList<>();

}
