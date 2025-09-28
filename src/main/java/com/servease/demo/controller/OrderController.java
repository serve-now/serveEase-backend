package com.servease.demo.controller;

import com.servease.demo.dto.request.OrderCreateRequest;
import com.servease.demo.dto.request.OrderItemRequest;
import com.servease.demo.dto.response.OrderResponse;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.repository.RestaurantTableRepository;
import com.servease.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;
    private final RestaurantTableRepository restaurantTableRepository;

    @Autowired
    public OrderController(OrderService orderService, RestaurantTableRepository restaurantTableRepository) {
        this.orderService = orderService;
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @PostMapping("/stores/{storeId}/orders")
    public ResponseEntity<OrderResponse> createOrder(@PathVariable Long storeId,
                                                     @RequestBody @Valid OrderCreateRequest request) {
        OrderResponse orderResponse = orderService.createOrder(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @PostMapping("/orders/{orderId}/pay")
    public ResponseEntity<OrderResponse> completePayment(@PathVariable Long orderId) {
        OrderResponse paidOrder = orderService.completePayment(orderId);
        return ResponseEntity.ok(paidOrder);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestParam(required = false) OrderStatus status) {
        List<OrderResponse> orderResponses = orderService.getAllOrders(status);
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 새로운 메뉴 항목들을 주문에 추가
    @PostMapping("/orders/{orderId}/items")
    public ResponseEntity<OrderResponse> addItemsToOrder(@PathVariable Long orderId, @RequestBody @Valid List<OrderItemRequest> orderItemRequests) {
        OrderResponse updatedOrder = orderService.addItemsToOrder(orderId, orderItemRequests);
        return ResponseEntity.ok(updatedOrder);
    }


    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrderById(@PathVariable Long orderId) {
        OrderResponse canceledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
    }

    @DeleteMapping("/orders/{orderId}/items/{orderItemId}")
    public ResponseEntity<OrderResponse> removeOrderItem(@PathVariable Long orderId, @PathVariable Long orderItemId) {
        OrderResponse updatedOrder = orderService.removeOrderItem(orderId, orderItemId);
        return ResponseEntity.ok(updatedOrder);
    }



}
