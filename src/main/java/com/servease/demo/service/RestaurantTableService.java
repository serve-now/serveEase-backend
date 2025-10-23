package com.servease.demo.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
            RestaurantTable newTable = RestaurantTable.builder()
                    .tableNumber(i)
                    .store(store)
                    .status(RestaurantTableStatus.EMPTY)
                    .build();
            tables.add(newTable);
        }
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

        boolean hasOccupiedTables = currentTables.stream()
                .anyMatch(table -> table.getStatus() != RestaurantTableStatus.EMPTY);

        if (hasOccupiedTables) {
            throw new BusinessException(ErrorCode.TABLES_NOT_EMPTY, "사용 중인 테이블이 있어 작업할 수 없습니다.");
        }

        int currentCount = currentTables.size();

        if (newTotalCount < currentCount) {
            // 테이블 수를 줄이는 경우
            int tablesToRemoveCount = currentCount - newTotalCount;
            List<RestaurantTable> tablesToDelete = currentTables.stream()
                    .sorted(Comparator.comparing(RestaurantTable::getTableNumber).reversed())
                    .limit(tablesToRemoveCount)
                    .toList();

            // 삭제할 테이블과 연결된 모든 주문(Order)의 참조를 끊음
            for (RestaurantTable table : tablesToDelete) {
                severTiesWithOrders(table);
            }

            restaurantTableRepository.deleteAll(tablesToDelete);

        } else if (newTotalCount > currentCount) {
            //테이블 수를 늘리는 경우
            int tablesToAddCount = newTotalCount - currentCount;
            int maxTableNumber = currentTables.stream()
                    .mapToInt(RestaurantTable::getTableNumber)
                    .max()
                    .orElse(0);

            List<RestaurantTable> newTables = IntStream.range(1, tablesToAddCount + 1)
                    .mapToObj(i -> RestaurantTable.builder()
                            .store(store)
                            .tableNumber(maxTableNumber + i)
                            .status(RestaurantTableStatus.EMPTY)
                            .build())
                    .toList();
            restaurantTableRepository.saveAll(newTables);
        }
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

        List<Long> tableIds = tablePage.getContent().stream()
                .map(RestaurantTable::getId)
                .toList();

        List<OrderStatus> activeStatuses = List.of(OrderStatus.ORDERED, OrderStatus.SERVED, OrderStatus.PARTIALLY_PAID);
        List<Order> activeOrders = orderRepository.findByRestaurantTableIdInAndStatusInOrderByOrderTimeDesc(tableIds, activeStatuses);

        Map<Long, Order> latestOrderMap = activeOrders.stream()
                .collect(Collectors.toMap(
                        order -> order.getRestaurantTable().getId(),
                        Function.identity(),
                        (existing, replacement) -> existing // 중복 키 발생 시 기존 값 유지 (이미 시간 역순 정렬)
                ));

        List<RestaurantTableResponse> dtos = tablePage.getContent().stream()
                .map(table -> {
                    Order latestOrder = latestOrderMap.get(table.getId());
                    return RestaurantTableResponse.from(table, latestOrder);
                })
                .toList();

        return new PageImpl<>(dtos, pageable, tablePage.getTotalElements());
    }

    @Transactional
    public RestaurantTableResponse updateTableStatus(Long id, RestaurantTableStatus newStatus) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table with ID " + id + " not found."));
        table.updateStatus(newStatus);
        return RestaurantTableResponse.fromEntity(table);
    }

    @Transactional
    public void deleteTable(Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table with ID " + tableId + " not found."));
        severTiesWithOrders(table);
        restaurantTableRepository.delete(table);
    }


    private void severTiesWithOrders(RestaurantTable table) {
        List<Order> linkedOrders = orderRepository.findByRestaurantTableId(table.getId());
        if (!linkedOrders.isEmpty()) {
            linkedOrders.forEach(order -> order.setRestaurantTable(null));
            orderRepository.saveAllAndFlush(linkedOrders);
        }
    }
}
