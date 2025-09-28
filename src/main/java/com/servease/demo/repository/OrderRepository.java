package com.servease.demo.repository;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRestaurantTableId(Long restaurantTableId);

    List<Order> findByRestaurantTableIdAndStatusIn(Long restaurantTableId, Collection<OrderStatus> statuses);

    // active : ORDERED, IN_PROGRESS
    // inactive : CANCELED
    List<Order> findAllByStatusIn(Collection<OrderStatus> statuses);
}