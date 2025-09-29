package com.servease.demo.repository;

import com.servease.demo.model.entity.Order;
import com.servease.demo.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRestaurantTableId(Long restaurantTableId);

    List<Order> findByRestaurantTableIdAndStatusIn(Long restaurantTableId, Collection<OrderStatus> statuses);

    Page<Order> findByRestaurantTable_StoreIdAndStatus(Long storeId, OrderStatus status, Pageable pageable);

    Page<Order> findByRestaurantTable_StoreIdAndStatusIn(Long storeId, List<OrderStatus> statuses, Pageable pageable);

    // active : ORDERED, IN_PROGRESS
    // inactive : CANCELED
    List<Order> findAllByStatusIn(Collection<OrderStatus> statuses);
}