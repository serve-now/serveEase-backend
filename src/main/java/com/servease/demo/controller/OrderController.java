package com.servease.demo.controller;

import com.servease.demo.dto.request.CashPaymentRefundRequest;
import com.servease.demo.dto.request.CashPaymentRequest;
import com.servease.demo.dto.request.OrderItemRequest;
import com.servease.demo.dto.response.OrderResponse;
import com.servease.demo.dto.response.CashPaymentRefundResponse;
import com.servease.demo.dto.response.PaymentConfirmResponse;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.service.CashPaymentService;
import com.servease.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final CashPaymentService cashPaymentService;

    @PostMapping("/{orderId}/payments/cash")
    public ResponseEntity<PaymentConfirmResponse> payCash(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @Valid @RequestBody CashPaymentRequest request
    ) {
        log.info("[ORDER] cash payment request storeId={}, orderId={}, amount={}", storeId, orderId, request.amount());
        PaymentConfirmResponse response = cashPaymentService.applyCashPayment(orderId, request.amount());
        log.info("[ORDER] cash payment success storeId={}, orderId={}, remainingAmount={}", storeId, orderId, response.remainingAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/payments/cash/full")
    public ResponseEntity<PaymentConfirmResponse> payCashFully(
            @PathVariable Long storeId,
            @PathVariable Long orderId
    ) {
        log.info("[ORDER] cash payment full request storeId={}, orderId={}", storeId, orderId);
        PaymentConfirmResponse response = cashPaymentService.completeOutstandingPayment(orderId);
        log.info("[ORDER] cash payment full success storeId={}, orderId={}, remainingAmount={}", storeId, orderId, response.remainingAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/payments/cash/refund")
    public ResponseEntity<CashPaymentRefundResponse> refundCash(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @Valid @RequestBody CashPaymentRefundRequest request
    ) {
        log.info("[ORDER] cash refund request storeId={}, orderId={}, cashPaymentId={}, amount={}",
                storeId, orderId, request.cashPaymentId(), request.refundAmount());
        CashPaymentRefundResponse response = cashPaymentService.refundCashPayment(orderId, request);
        log.info("[ORDER] cash refund success storeId={}, orderId={}, cashPaymentId={}, paidAmount={}, remainingAmount={}",
                storeId, orderId, response.cashPaymentId(), response.paidAmount(), response.remainingAmount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long storeId,
            @PathVariable Long orderId) {
        log.info("[ORDER] get order request storeId={}, orderId={}", storeId, orderId);
        OrderResponse orderResponse = orderService.getOrderById(orderId);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrdersByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        log.info("[ORDER] list orders request storeId={}, status={}, pageNumber={}, pageSize={}",
                storeId, status, pageable.getPageNumber(), pageable.getPageSize());
        Page<OrderResponse> orderResponses = orderService.getOrdersByStore(storeId, status, pageable);
        return ResponseEntity.ok(orderResponses);
    }

    // 새로운 메뉴 항목들을 주문에 추가
    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addItemsToOrder(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @RequestBody @Valid List<OrderItemRequest> orderItemRequests) {
        log.info("[ORDER] add items request storeId={}, orderId={}, itemsCount={}",
                storeId, orderId, orderItemRequests == null ? 0 : orderItemRequests.size());
        OrderResponse updatedOrder = orderService.addItemsToOrder(orderId, orderItemRequests);
        return ResponseEntity.ok(updatedOrder);
    }


    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrderById(
            @PathVariable Long storeId,
            @PathVariable Long orderId) {
        log.info("[ORDER] cancel request storeId={}, orderId={}", storeId, orderId);
        OrderResponse canceledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<OrderResponse> removeOrderItem(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @PathVariable Long orderItemId) {
        log.info("[ORDER] remove item request storeId={}, orderId={}, orderItemId={}",
                storeId, orderId, orderItemId);
        OrderResponse updatedOrder = orderService.removeOrderItem(orderId, orderItemId);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{orderId}/serve")
    public ResponseEntity<OrderResponse> markAsServed(@PathVariable Long orderId) {
        log.info("[ORDER] mark as served request orderId={}", orderId);
        OrderResponse servedOrder = orderService.markOrderAsServed(orderId);
        return ResponseEntity.ok(servedOrder);
    }




}
