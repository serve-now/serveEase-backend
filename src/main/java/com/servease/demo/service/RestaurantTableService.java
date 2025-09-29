package com.servease.demo.service;

import com.servease.demo.dto.response.ActiveOrderResponse;
import com.servease.demo.dto.response.RestaurantTableResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.repository.OrderRepository;
import com.servease.demo.repository.RestaurantTableRepository;
import com.servease.demo.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

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
    public void updateTableCount(Long storeId, int newTotalCount) {
        if (newTotalCount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "테이블 수는 1 이상이어야 합니다.");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        List<RestaurantTable> currentTables = restaurantTableRepository.findAllByStoreId(storeId);

        boolean allTablesAreEmpty = currentTables.stream()
                .allMatch(table -> table.getStatus() == RestaurantTableStatus.EMPTY);

        if (!allTablesAreEmpty) {
            throw new BusinessException(ErrorCode.TABLES_NOT_EMPTY, "사용 중인 테이블이 있어 작업할 수 없습니다.");
        }

        int currentCount = currentTables.size();

        if (newTotalCount < currentCount) {
            int tablesToRemoveCount = currentCount - newTotalCount;
            currentTables.stream()
                    .sorted(Comparator.comparing(RestaurantTable::getTableNumber).reversed())
                    .limit(tablesToRemoveCount)
                    .forEach(restaurantTableRepository::delete);

        } else if (newTotalCount > currentCount) {
            int tablesToAddCount = newTotalCount - currentCount;

            int maxTableNumber = currentTables.stream()
                    .mapToInt(RestaurantTable::getTableNumber)
                    .max()
                    .orElse(0);

            IntStream.range(1, tablesToAddCount + 1)
                    .mapToObj(i -> RestaurantTable.builder()
                            .store(store)
                            .tableNumber(maxTableNumber + i)
                            .status(RestaurantTableStatus.EMPTY)
                            .build())
                    .forEach(restaurantTableRepository::save);
        }
        // newTotalCount == currentCount 인 경우는 아무 작업도 하지 않음
    }


    @Transactional
    public RestaurantTableResponse createTable(Long storeId, Integer tableNumber) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));


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

        List<RestaurantTableResponse> dtos = tablePage.getContent().stream()
                .map(t -> {
                    if (t.getStatus() == RestaurantTableStatus.EMPTY) {
                        return RestaurantTableResponse.builder()
                                .id(t.getId())
                                .restaurantTableNumber(t.getTableNumber())
                                .displayStatus("EMPTY")
                                .activeOrder(null)
                                .build();
                    }

                    Optional<Order> latestOrderOpt = orderRepository
                            .findTopByRestaurantTableIdOrderByOrderTimeDesc(t.getId());

                    if (latestOrderOpt.isEmpty()) {
                        return RestaurantTableResponse.builder()
                                .id(t.getId())
                                .restaurantTableNumber(t.getTableNumber())
                                .displayStatus("EMPTY")
                                .activeOrder(null)
                                .build();
                    }

                    Order latestOrder = latestOrderOpt.get();
                    OrderStatus latestStatus = latestOrder.getStatus();

                    String displayStatus;
                    ActiveOrderResponse activeOrderResponse = null;

                    switch (latestStatus) {
                        case ORDERED, SERVED -> {
                            displayStatus = latestStatus.name();
                            activeOrderResponse = ActiveOrderResponse.fromEntity(latestOrder);
                        }
                        case COMPLETED, CANCELED -> {
                            displayStatus = "EMPTY";
                        }
                        default -> {
                            displayStatus = "EMPTY";
                        }
                    }

                    return RestaurantTableResponse.builder()
                            .id(t.getId())
                            .restaurantTableNumber(t.getTableNumber())
                            .displayStatus(displayStatus)
                            .activeOrder(activeOrderResponse)
                            .build();
                })
                .toList();

        return new PageImpl<>(dtos, pageable, tablePage.getTotalElements());
    }





    @Transactional
    public RestaurantTableResponse updateTableStatus(Long id, RestaurantTableStatus newStatus) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table with ID " + id + " not found."));
        table.setStatus(newStatus);
        return RestaurantTableResponse.fromEntity(table);
    }


    @Transactional
    public void deleteTable(Long storeId, Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table with ID " + tableId + " not found."));
        if (!table.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This table does not belong to the specified store.");
        }
        restaurantTableRepository.delete(table);
    }
}
