package com.servease.demo.repository;

import com.servease.demo.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRestaurantTableId(Long restaurantTableId);

    //테이블당 활성 주문은 하나
    Optional<Order> findByRestaurantTableIdAndIsPaidFalse (Long restaurantTableId);
}