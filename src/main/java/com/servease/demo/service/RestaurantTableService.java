package com.servease.demo.service;

import com.servease.demo.dto.response.RestaurantTableResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional
    public void createTablesForStore(Store store, int tableCount) {
        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException("Store must be saved before creating tables.");
        }

        List<RestaurantTable> tables = new ArrayList<>();
        for (int i = 1; i <= tableCount; i++) {
            //새로운 가게에 대해서만 호출되므로 중복될 일 x
            RestaurantTable newTable = RestaurantTable.builder()
                    .tableNumber(i)
                    .store(store)
                    .status(RestaurantTableStatus.EMPTY)
                    .build();
            tables.add(newTable);
        }
        // 생성된 테이블 리스트를 한 번에 저장 (JPA가 최적화된 쿼리 실행해줌)
        restaurantTableRepository.saveAll(tables);
    }


    @Transactional
    public RestaurantTableResponse createTable(Integer tableNumber, Store store) {
        if (restaurantTableRepository.findByStoreIdAndTableNumber(store.getId(), tableNumber).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_TABLE_NUMBER, "Table number " + tableNumber + " already exists in store " + store.getId());
        }

        RestaurantTable newTable = RestaurantTable.builder()
                .tableNumber(tableNumber)
                .store(store)
                .status(RestaurantTableStatus.EMPTY)
                .build();

        RestaurantTable savedTable = restaurantTableRepository.save(newTable);
        return RestaurantTableResponse.fromEntity(savedTable);
    }


    public Page<RestaurantTableResponse> getAllTablesByStore(Long storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("tableNumber").ascending());
        Page<RestaurantTable> tablePage = restaurantTableRepository.findAllByStoreId(storeId, pageable);

        if (!tablePage.hasContent()) {
            return Page.empty(pageable);
        }

        List<Long> tableIds = tablePage.getContent().stream()
                .map(RestaurantTable::getId)
                .collect(Collectors.toList());

        List<RestaurantTable> tablesWithDetails = restaurantTableRepository.findAllWithActiveOrdersByIds(tableIds);

        List<RestaurantTableResponse> dtos = tablesWithDetails.stream()
                .map(RestaurantTableResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, tablePage.getTotalElements());
    }

    public RestaurantTableResponse getTableById(Long id) {
        return restaurantTableRepository.findById(id)
                .map(RestaurantTableResponse::fromEntity)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table with ID " + id + " not found."));
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
