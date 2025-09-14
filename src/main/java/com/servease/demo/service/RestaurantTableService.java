package com.servease.demo.service;

import com.servease.demo.dto.response.RestaurantTableResponse;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional
    public RestaurantTableResponse createTable(Integer tableNumber) {
        if (restaurantTableRepository.findByTableNumber(tableNumber).isPresent()) {
            throw new IllegalArgumentException("Table number " + tableNumber + "already Exists");
        }

        RestaurantTable newTable = RestaurantTable.builder()
                .tableNumber(tableNumber)
                .status(RestaurantTableStatus.EMPTY)
                .build();

        RestaurantTable savedTable = restaurantTableRepository.save(newTable);
        return RestaurantTableResponse.fromEntity(savedTable);
    }

    public RestaurantTableResponse getTableById(Long id) {
        return restaurantTableRepository.findById(id)
                .map(RestaurantTableResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + id));
    }

    public List<RestaurantTableResponse> getAllTables() {
        return restaurantTableRepository.findAll().stream()
                .map(RestaurantTableResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantTableResponse updateTableStatus(Long id, RestaurantTableStatus newStatus) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID:" + id));
        table.setStatus(newStatus);
        return RestaurantTableResponse.fromEntity(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        restaurantTableRepository.deleteById(id);
    }
}
