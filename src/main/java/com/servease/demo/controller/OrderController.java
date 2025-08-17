package com.servease.demo.controller;

import com.servease.demo.dto.request.OrderCreateRequest;
import com.servease.demo.dto.response.OrderResponse;
import com.servease.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderCreateRequest request){
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders (){
        List<OrderResponse> orderResponses = orderService.getAllOrders().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderResponses);
    }

}
