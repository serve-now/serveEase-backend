package com.servease.demo.controller;

import com.servease.demo.dto.request.OrderCreateRequest;
import com.servease.demo.dto.request.RestaurantTableCreateRequest;
import com.servease.demo.dto.request.RestaurantTableStatusUpdateRequest;
import com.servease.demo.dto.request.TableCountUpdateRequest;
import com.servease.demo.dto.response.OrderResponse;
import com.servease.demo.dto.response.RestaurantTableResponse;
import com.servease.demo.repository.StoreRepository;
import com.servease.demo.service.OrderService;
import com.servease.demo.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/stores/{storeId}/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final OrderService orderService;
    private final RestaurantTableService restaurantTableService;
    private final StoreRepository storeRepository;

    @Autowired
    public RestaurantTableController(RestaurantTableService restaurantTableService, OrderService orderService, StoreRepository storeRepository) {
        this.restaurantTableService = restaurantTableService;
        this.orderService = orderService;
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantTableResponse>> getAllTables(@PathVariable Long storeId,
                                                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                                                      @RequestParam(value = "size", defaultValue = "12") int size) {
        Page<RestaurantTableResponse> tablePage = restaurantTableService.getAllTablesByStore(storeId, page, size);
        return ResponseEntity.ok(tablePage);
    }


    @PostMapping("/{tableId}/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestBody @Valid OrderCreateRequest request) {
        OrderResponse newOrder = orderService.createOrder(storeId, tableId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    @PostMapping
    public ResponseEntity<RestaurantTableResponse> createTable(
            @PathVariable Long storeId,
            @RequestBody RestaurantTableCreateRequest request) {
        RestaurantTableResponse newTable = restaurantTableService.createTable(storeId, request.getRestaurantTableNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(newTable);
    }

    @PutMapping("/{tableId}")
    public ResponseEntity<RestaurantTableResponse> updateTableStatus(@PathVariable Long tableId, @RequestBody RestaurantTableStatusUpdateRequest request) {
        RestaurantTableResponse updatedTable = restaurantTableService.updateTableStatus(tableId, request.getStatus());
        return ResponseEntity.ok(updatedTable);
    }

    @DeleteMapping("/{tableId}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        restaurantTableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tableId}/orders")
    public ResponseEntity<Void> deleteAllOrdersByTable(
            @PathVariable Long storeId,
            @PathVariable Long tableId) {
        orderService.deleteAllOrdersByTable(storeId, tableId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bulk-update")
    public ResponseEntity<Void> updateTableCount(
            @PathVariable Long storeId,
            @Valid @RequestBody TableCountUpdateRequest request) {

        restaurantTableService.updateTableCount(storeId, request.getNewTotalCount());
        return ResponseEntity.noContent().build();
    }


}