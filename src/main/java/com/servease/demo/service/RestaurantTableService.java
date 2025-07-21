package com.servease.demo.service;

import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.repository.RestaurantTableRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    @Autowired
    public RestaurantTableService(RestaurantTableRepository restaurantTableRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Transactional
    public RestaurantTable createTable(Integer tableNumber) {
        if (restaurantTableRepository.findByTableNumber(tableNumber).isPresent()) {
            throw new IllegalArgumentException("Table number " + tableNumber + "already Exists");
        }

        RestaurantTable newTable = RestaurantTable.builder()
                .tableNumber(tableNumber)
                .status(RestaurantTableStatus.EMPTY)
                .build();

        return restaurantTableRepository.save(newTable);
    }

    public Optional<RestaurantTable> getTableById(Long id) {
        return restaurantTableRepository.findById(id);
    }

    public List<RestaurantTable> getAllTables() {
        return restaurantTableRepository.findAll();
    }

    @Transactional
    public RestaurantTable updateTableStatus(Long id, RestaurantTableStatus newStatus) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID:" + id));
        table.updateStatus(newStatus);
        return restaurantTableRepository.save(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        restaurantTableRepository.deleteById(id);
    }
}
