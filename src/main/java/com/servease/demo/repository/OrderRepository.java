package com.servease.demo.repository;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRestaurantTableId(Long restaurantTableId);

    Optional<Order> findByRestaurantTableIdAndStatusIn(Long restaurantTableId, Collection<OrderStatus> statuses);

    // active : RECEIVED, IN_PROGRESS
    // inactive : CANCELED
    List<Order> findAllByStatusIn(Collection<OrderStatus> statuses);
}