package com.example.commerce_mvp.domain.order;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("주문 대기"),
    CONFIRMED("주문 확인"),
    PREPARING("상품 준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("주문 취소"),
    REFUNDED("환불완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
