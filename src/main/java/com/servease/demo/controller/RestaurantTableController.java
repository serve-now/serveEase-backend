package com.servease.demo.controller;

import com.servease.demo.dto.request.RestaurantTableCreateRequest;
import com.servease.demo.dto.response.RestaurantTableResponse;
import com.servease.demo.service.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    @Autowired
    public RestaurantTableController(RestaurantTableService restaurantTableService) {
        this.restaurantTableService = restaurantTableService;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantTableResponse>> getAllTables(){
        List<RestaurantTableResponse> tableResponses = restaurantTableService.getAllTables();
        return ResponseEntity.ok(tableResponses);
    }


    @PostMapping
    public ResponseEntity<RestaurantTableResponse> createTable(@RequestBody RestaurantTableCreateRequest request) {
        RestaurantTableResponse newTable = restaurantTableService.createTable(request.getRestaurantTableNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(newTable);
    }

}