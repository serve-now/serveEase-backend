package com.servease.demo.controller;

import com.servease.demo.dto.request.RestaurantTableCreateRequest;
import com.servease.demo.dto.request.RestaurantTableStatusUpdateRequest;
import com.servease.demo.dto.response.RestaurantTableResponse;
import com.servease.demo.service.OrderService;
import com.servease.demo.service.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final OrderService orderService;
    private final RestaurantTableService restaurantTableService;

    @Autowired
    public RestaurantTableController(RestaurantTableService restaurantTableService, OrderService orderService) {
        this.restaurantTableService = restaurantTableService;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantTableResponse>> getAllTables(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                      @RequestParam(value = "size", defaultValue = "12") int size) {
        Page<RestaurantTableResponse> tablePage = restaurantTableService.getAllTables(page, size);
        return ResponseEntity.ok(tablePage);
    }


    @PostMapping
    public ResponseEntity<RestaurantTableResponse> createTable(@RequestBody RestaurantTableCreateRequest request) {
        RestaurantTableResponse newTable = restaurantTableService.createTable(request.getRestaurantTableNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(newTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableResponse> updateTableStatus(@PathVariable Long id, @RequestBody RestaurantTableStatusUpdateRequest request) {
        RestaurantTableResponse updatedTable = restaurantTableService.updateTableStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedTable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        restaurantTableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tableId}/orders")
    public ResponseEntity<Void> deleteAllOrdersByTable(@PathVariable Long tableId) {
        orderService.deleteAllOrdersByTable(tableId);
        return ResponseEntity.noContent().build();
    }
}