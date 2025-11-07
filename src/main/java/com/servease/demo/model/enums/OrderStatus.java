package com.servease.demo.model.enums;

//주문 전체
public enum OrderStatus {
    CANCELED,
    ORDERED,
    SERVED, // 음식이 서빙되었다면 SERVED 로 변경되는 독립적인 상태
    PARTIALLY_PAID, // 일부 금액이 결제된 상태
    COMPLETED, // 전액 결제로 주문이 확정된 상태
    REFUNDED // 결제가 환불되어 금액이 되돌려진 상태
}
