package com.servease.demo.model.enums;

//주문 전체
public enum OrderStatus {
    CANCELED,
    ORDERED,
    SERVED, // SERVED : paid 와 무관, 음식이 서빙되었다면 SERVED 로 변경되는 독립적인 상태
    COMPLETED, // isPaid = true, OrderStatus = SERVED 두 가지의 상태를 만족할 때 COMPLETED 로 변경됨
}
