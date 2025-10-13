package com.servease.demo.controller;

import com.servease.demo.dto.request.OrderItemRequest;
import com.servease.demo.dto.response.OrderResponse;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.repository.RestaurantTableRepository;
import com.servease.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService, RestaurantTableRepository restaurantTableRepository) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> completePayment(
            @PathVariable Long storeId,
            @PathVariable Long orderId) {
        OrderResponse paidOrder = orderService.completePayment(orderId);
        return ResponseEntity.ok(paidOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long storeId,
            @PathVariable Long orderId) {
        OrderResponse orderResponse = orderService.getOrderById(orderId);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrdersByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        Page<OrderResponse> orderResponses = orderService.getOrdersByStore(storeId, status, pageable);
        return ResponseEntity.ok(orderResponses);
    }

    // 새로운 메뉴 항목들을 주문에 추가
    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addItemsToOrder(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @RequestBody @Valid List<OrderItemRequest> orderItemRequests) {
        OrderResponse updatedOrder = orderService.addItemsToOrder(orderId, orderItemRequests);
        return ResponseEntity.ok(updatedOrder);
    }


    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrderById(
            @PathVariable Long storeId,
            @PathVariable Long orderId) {
        OrderResponse canceledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<OrderResponse> removeOrderItem(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @PathVariable Long orderItemId) {
        OrderResponse updatedOrder = orderService.removeOrderItem(orderId, orderItemId);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{orderId}/serve")
    public ResponseEntity<OrderResponse> markAsServed(@PathVariable Long orderId) {
        OrderResponse servedOrder = orderService.markOrderAsServed(orderId);
        return ResponseEntity.ok(servedOrder);
    }




}
