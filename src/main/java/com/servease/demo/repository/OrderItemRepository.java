package com.servease.demo.repository;

import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByMenuIdAndOrder_StatusIn(Long menuId, Iterable<OrderStatus> statuses);
}
