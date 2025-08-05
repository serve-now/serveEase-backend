package com.servease.demo.controller;

import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.service.RestaurantTableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    public RestaurantTableController(RestaurantTableService restaurantTableService) {
        this.restaurantTableService = restaurantTableService;
    }

    @PostMapping
    public ResponseEntity<RestaurantTable> createTable(@RequestParam Integer tableNumber) {
        RestaurantTable newTable = restaurantTableService.createTable(tableNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTable);
    }
}