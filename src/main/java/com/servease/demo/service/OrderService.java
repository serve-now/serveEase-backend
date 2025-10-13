package com.servease.demo.service;

import com.servease.demo.dto.request.OrderCreateRequest;
import com.servease.demo.dto.request.OrderItemRequest;
import com.servease.demo.dto.response.OrderResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Menu;
import com.servease.demo.model.entity.Order;
import com.servease.demo.model.entity.OrderItem;
import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.repository.MenuRepository;
import com.servease.demo.repository.OrderRepository;
import com.servease.demo.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final MenuRepository menuRepository;


    @Transactional
    public OrderResponse createOrder(Long tableId, OrderCreateRequest request) {
        RestaurantTable targetTable = restaurantTableRepository.findByIdWithLock(tableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table ID " + tableId + " does not exist."));

        // TODO: exists query 로 개선하기 -> queryDSL
        List<Order> activeOrders = orderRepository.findByRestaurantTableIdAndStatusIn(targetTable.getId(), List.of(OrderStatus.ORDERED, OrderStatus.SERVED));
        if (!activeOrders.isEmpty()) {
            throw new BusinessException(ErrorCode.ACTIVE_ORDER_EXISTS,
                    "An active order already exists for table " + targetTable.getTableNumber());
        }

        String orderId = UUID.randomUUID().toString();
        Order newOrder = Order.builder()
                .orderId(orderId)
                .restaurantTable(targetTable)
                .status(OrderStatus.ORDERED)
                .isPaid(false)
                .build();

        List<Long> menuIds = request.getOrderItems().stream()
                .map(OrderItemRequest::getMenuId)
                .collect(Collectors.toList());

        List<Menu> menus = menuRepository.findAllByIdInAndAvailableIsTrue(menuIds);

        Map<Long, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuMap.get(itemRequest.getMenuId());

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .quantity(itemRequest.getQuantity())
                    .itemPrice(menu.getPrice())
                    .build();

            newOrder.addOrderItem(orderItem);
        }

        newOrder.calculateTotalPrice();

        Order savedOrder = orderRepository.save(newOrder);
        targetTable.updateStatus(RestaurantTableStatus.USING);
        return OrderResponse.fromEntity(savedOrder);
    }


    public Page<OrderResponse> getOrdersByStore(Long storeId, OrderStatus status, Pageable pageable) {
        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByRestaurantTable_StoreIdAndStatus(storeId, status, pageable);
        } else {
            List<OrderStatus> activeStatuses = List.of(OrderStatus.ORDERED, OrderStatus.SERVED);
            orderPage = orderRepository.findByRestaurantTable_StoreIdAndStatusIn(storeId, activeStatuses, pageable);
        }
        return orderPage.map(OrderResponse::fromEntity);
    }

    public OrderResponse getOrderById(Long storeId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getRestaurantTable().getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This table does not belong to the specified store.");
        }
        return OrderResponse.fromEntity(order);
    }


    public List<OrderResponse> getOrdersByRestaurantTableId(Long restaurantTableId) {
        return orderRepository.findByRestaurantTableId(restaurantTableId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }


    //수량 증감
    @Transactional
    public OrderResponse addItemsToOrder(Long storeId, Long orderId, List<OrderItemRequest> itemRequests) {
        Order order = findOrderAndVerifyOwnership(storeId, orderId);

        if (!order.getRestaurantTable().getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This table does not belong to the specified store.");
        }

        //여기서 status canceled 가 없으면 주문 삭제 후에도 add 가 되는지 test 해봐야 함
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "Cannot add items to a completed or canceled order");
        }

        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "No items to add");
        }

        for (OrderItemRequest itemRequest : itemRequests) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND, "Menu item not found with ID: " + itemRequest.getMenuId()));

            if (!menu.isAvailable()) {
                throw new BusinessException(ErrorCode.MENU_NOT_AVAILABLE, " Menu item " + menu.getName() + "is not available.");
            }

            Optional<OrderItem> existingItemOpt = order.getOrderItems().stream()
                    .filter(item -> item.getMenu().getId().equals(itemRequest.getMenuId()))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                OrderItem existingItem = existingItemOpt.get();
                int newQuantity = existingItem.getQuantity() + itemRequest.getQuantity();

                if (newQuantity > 0) {
                    existingItem.setQuantity(newQuantity);
                } else {
                    order.removeOrderItem(existingItem);
                }

            } else { //새로운 아이템을 추가하는 경우
                if (itemRequest.getQuantity() > 0) {
                    OrderItem newOrderItem = OrderItem.builder()
                            .menu(menu)
                            .quantity(itemRequest.getQuantity())
                            .itemPrice(menu.getPrice())
                            .build();
                    order.addOrderItem(newOrderItem);
                } else {
                    // 새로운 아이템을 추가하는데 -> 아이템 수량이 음수이거나 0인 경우 에러 던지기
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                            "Cannot add a new item with zero or negative quantity: " + menu.getName());
                }
            }
        }

        if (order.getStatus() == OrderStatus.SERVED) {
            order.setStatus(OrderStatus.ORDERED);
        }

        order.calculateTotalPrice();

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }


    @Transactional
    public OrderResponse removeOrderItem(Long storeId, Long orderId, Long orderItemId) {
        Order order = findOrderAndVerifyOwnership(storeId, orderId);
        order.removeItemById(orderItemId);
        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }


    @Transactional
    public OrderResponse cancelOrder(Long storeId, Long orderId) {
        Order order = findOrderAndVerifyOwnership(storeId, orderId);

        if (!order.getRestaurantTable().getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This table does not belong to the specified store.");
        }

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "Cannot cancel a completed order or already canceled order.");
        }

        order.setStatus(OrderStatus.CANCELED);

        RestaurantTable restaurantTable = order.getRestaurantTable();
        restaurantTable.updateStatus(RestaurantTableStatus.EMPTY);

        restaurantTableRepository.save(restaurantTable);
        Order canceledOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(canceledOrder);
    }


    //선결제 하는 경우는 payment 와 분리
    @Transactional
    public OrderResponse completePayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found with ID :" + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID, "Status cannot be change, current status : " + order.getStatus());
        }
        if (order.isPaid()) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }

        order.setPaid(true);
        order.setStatus(OrderStatus.COMPLETED);

        //결제가 완료되면 주문이 종결 -> 테이블 상태를 EMPTY 로 변경
        RestaurantTable table = order.getRestaurantTable();
        if (table.getStatus() == RestaurantTableStatus.USING) {
            table.updateStatus(RestaurantTableStatus.EMPTY);
        }

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }


    @Transactional
    public void deleteAllOrdersByTable(Long storeId, Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "RestaurantTable not found with ID: " + tableId));

        if (!table.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This table does not belong to the specified store.");
        }

        List<Order> ordersToCancel = orderRepository.findByRestaurantTableId(tableId);
        if (ordersToCancel.isEmpty()) {
            table.updateStatus(RestaurantTableStatus.EMPTY);
            return;
        }

        for (Order order : ordersToCancel) {
            if (order.getStatus() != OrderStatus.COMPLETED && order.getStatus() != OrderStatus.CANCELED) {
                order.setStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
            }
        }

        table.updateStatus(RestaurantTableStatus.EMPTY);
        restaurantTableRepository.save(table);
    }

    @Transactional
    public OrderResponse markOrderAsServed(Long storeId, Long orderId) {
        Order order = findOrderAndVerifyOwnership(storeId, orderId);

        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "Order status must be ORDERED to be marked as SERVED. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.SERVED);

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }


    private Order findOrderAndVerifyOwnership(Long storeId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        if (!order.getRestaurantTable().getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This table does not belong to the specified store.");
        }
        return order;
    }

}
