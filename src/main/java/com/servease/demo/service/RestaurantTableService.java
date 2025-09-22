package com.servease.demo.service;

import com.servease.demo.dto.response.RestaurantTableResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    public Page<RestaurantTableResponse> getAllTables(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<RestaurantTable> tablePage = restaurantTableRepository.findAll(pageable);
        return tablePage.map(RestaurantTableResponse::fromEntity);
    }

    public RestaurantTableResponse getTableById(Long id) {
        return restaurantTableRepository.findById(id)
                .map(RestaurantTableResponse::fromEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table with ID " + id + " not found."));
    }

    @Transactional
    public RestaurantTableResponse createTable(Integer tableNumber, Store store) {
        if (restaurantTableRepository.findByStoreAndTableNumber(store, tableNumber).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_TABLE_NUMBER, "Table number " + tableNumber + " already exists.");
        }

        RestaurantTable newTable = RestaurantTable.builder()
                .tableNumber(tableNumber)
                .store(store)
                .status(RestaurantTableStatus.EMPTY)
                .build();

        RestaurantTable savedTable = restaurantTableRepository.save(newTable);
        return RestaurantTableResponse.fromEntity(savedTable);
    }


    @Transactional
    public RestaurantTableResponse updateTableStatus(Long id, RestaurantTableStatus newStatus) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table with ID " + id + " not found."));
        table.setStatus(newStatus);
        return RestaurantTableResponse.fromEntity(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        restaurantTableRepository.deleteById(id);
    }
}
