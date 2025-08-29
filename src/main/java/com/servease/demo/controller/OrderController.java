package com.servease.demo.controller;

import com.servease.demo.dto.request.OrderCreateRequest;
import com.servease.demo.dto.request.OrderItemRequest;
import com.servease.demo.dto.request.OrderItemQuantityUpdateRequest;
import com.servease.demo.dto.response.OrderResponse;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    //create
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestParam(required = false) OrderStatus status) {
        List<OrderResponse> orderResponses = orderService.getAllOrders(status);
        return ResponseEntity.ok(orderResponses);
    }


    // 새로운 메뉴 항목들을 주문에 추가
    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addItemsToOrder(@PathVariable Long orderId, @RequestBody @Valid List<OrderItemRequest> orderItemRequests) {
        OrderResponse updatedOrder = orderService.addItemsToOrder(orderId, orderItemRequests);
        return ResponseEntity.ok(updatedOrder);
    }

    // 기존 주문 항목의 수량을 변경 (Update)
    @PatchMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<OrderResponse> updateOrderItemQuantity(@PathVariable Long orderId, @PathVariable Long orderItemId, @RequestBody @Valid OrderItemQuantityUpdateRequest request) {
        OrderResponse updatedOrder = orderService.updateOrderItemQuantity(orderId, orderItemId, request.getNewQuantity());
        return ResponseEntity.ok(updatedOrder);
    }


}
