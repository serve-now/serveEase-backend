package com.servease.demo.service;

import com.servease.demo.model.entity.Menu;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.OrderItemStatus;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.repository.MenuRepository;
import com.servease.demo.repository.OrderRepository;
import com.servease.demo.repository.RestaurantTableRepository;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final MenuRepository menuRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, RestaurantTableRepository restaurantTableRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.restaurantTableRepository = restaurantTableRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public Order createOrder(Long tableId, Map<Long, Integer> menuItemsMap) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + tableId));
        table.updateStatus(RestaurantTableStatus.EATING);

        //새로운 Order 엔티티 생성
        Order newOrder = Order.builder()
                .table(table)
                .status(OrderStatus.RECEIVED)
                .orderTime(LocalDateTime.now())
                .build();

        if (menuItemsMap == null || menuItemsMap.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least 1 menu");
        }

        int totalOrderPrice = 0;
        for (Map.Entry<Long, Integer> entry : menuItemsMap.entrySet()) {
            Long menuId = entry.getKey();
            Integer quantity = entry.getValue();

            Menu menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found with ID" + menuId));

            if (!menu.getIsAvailable() || quantity <= 0) {
                throw new IllegalArgumentException("Invalid menu item or quantity");
            }

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .quantity(quantity)
                    .itemPrice(menu.getPrice())
                    .status(OrderItemStatus.IN_COOKING)
                    .build();

            newOrder.addOrderItem(orderItem);
            totalOrderPrice += (quantity * menu.getPrice());
        }

        newOrder.setTotalPrice(totalOrderPrice);
        return orderRepository.save(newOrder);
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }


}
