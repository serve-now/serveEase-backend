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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final MenuRepository menuRepository;


    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        RestaurantTable initialTable = restaurantTableRepository.findByTableNumber(request.getRestaurantTableNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "Table number " + request.getRestaurantTableNumber() + " does not exist."));

        RestaurantTable targetTable = restaurantTableRepository.findByIdWithLock(initialTable.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Could not acquire lock for table: " + initialTable.getTableNumber()));

        orderRepository.findByRestaurantTableIdAndStatusIn(targetTable.getId(), List.of(OrderStatus.RECEIVED, OrderStatus.SERVED));
        Optional<Order> activeOrderOpt = orderRepository.findByRestaurantTableIdAndStatusIn(targetTable.getId(), List.of(OrderStatus.RECEIVED, OrderStatus.SERVED));

        if (activeOrderOpt.isPresent()) {
            throw new BusinessException(ErrorCode.ACTIVE_ORDER_EXISTS,
                    "An active order already exists for table " + targetTable.getTableNumber());
        }


        Order newOrder = Order.builder()
                .restaurantTable(targetTable)
                .status(OrderStatus.RECEIVED)
                .isPaid(false)
                .build();

        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND, "Menu item not found with ID" + itemRequest.getMenuId()));

            if (!menu.isAvailable()) {
                throw new BusinessException(ErrorCode.MENU_NOT_AVAILABLE, "Invalid menu item or quantity");
            }

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .quantity(itemRequest.getQuantity())
                    .itemPrice(menu.getPrice())
                    .build();

            newOrder.addOrderItem(orderItem);
        }
        newOrder.calculateTotalPrice();

        Order savedOrder = orderRepository.save(newOrder);
        return OrderResponse.fromEntity(savedOrder);
    }


    public List<OrderResponse> getAllOrders(OrderStatus status) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findAllByStatusIn(List.of(status));
        } else {
            orders = orderRepository.findAllByStatusIn(List.of(OrderStatus.RECEIVED, OrderStatus.SERVED));
        }

        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id).map(OrderResponse::fromEntity);
    }


    public List<OrderResponse> getOrdersByRestaurantTableId(Long restaurantTableId) {
        return orderRepository.findByRestaurantTableId(restaurantTableId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }


    //수량 증감
    @Transactional
    public OrderResponse addItemsToOrder(Long orderId, List<OrderItemRequest> itemRequests) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found with ID" + orderId));

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

            if(existingItemOpt.isPresent()) {
                OrderItem existingItem = existingItemOpt.get();
                int newQuantity = existingItem.getQuantity() + itemRequest.getQuantity();

                if(newQuantity > 0) {
                    existingItem.setQuantity(newQuantity);
                } else {
                    order.removeOrderItem(existingItem);
                }

            } else { //새로운 아이템을 추가하는 경우
                if(itemRequest.getQuantity() > 0) {
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
            order.setStatus(OrderStatus.RECEIVED);
        }

        order.calculateTotalPrice();

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }


    @Transactional
    public OrderResponse removeOrderItem(Long orderId, Long orderItemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));
        order.removeItemById(orderItemId);

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }


    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

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
    public void deleteAllOrdersByTable(Long tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TABLE_NOT_FOUND, "RestaurantTable not found with ID: " + tableId));
        List<Order> ordersToCancel = orderRepository.findByRestaurantTableId(tableId);
        if (ordersToCancel.isEmpty()) {
            return;
        }
        for (Order order : ordersToCancel) {
            if (order.getStatus() != OrderStatus.COMPLETED && order.getStatus() != OrderStatus.CANCELED) {
                order.setStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
            }
        }

        table.setStatus(RestaurantTableStatus.EMPTY);
        restaurantTableRepository.save(table);
    }

    @Transactional
    public OrderResponse markOrderAsServed(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_VALID, "Order status must be RECEIVED to be marked as SERVED. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.SERVED);

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(updatedOrder);
    }

}
