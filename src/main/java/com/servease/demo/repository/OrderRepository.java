package com.servease.demo.repository;

import com.servease.demo.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRestaurantTableId(Long restaurantTableId);
}