package com.example.commerce_mvp.domain.order;

import com.example.commerce_mvp.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false)
    private String shippingPhone;

    @Column(nullable = false)
    private String shippingName;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    private LocalDateTime deliveryDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(User user, OrderStatus status, int totalAmount, String shippingAddress, 
                String shippingPhone, String shippingName) {
        this.user = user;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.shippingPhone = shippingPhone;
        this.shippingName = shippingName;
        this.orderDate = LocalDateTime.now();
    }

    // 주문 상태 변경
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
        if (newStatus == OrderStatus.DELIVERED) {
            this.deliveryDate = LocalDateTime.now();
        }
    }

    // 주문 아이템 추가
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 총 금액 계산
    public void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    // 주문 생성 팩토리 메서드
    public static Order createOrder(User user, String shippingAddress, String shippingPhone, String shippingName) {
        return Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(0) // 나중에 계산
                .shippingAddress(shippingAddress)
                .shippingPhone(shippingPhone)
                .shippingName(shippingName)
                .build();
    }

    // 주문 아이템 추가 및 재고 확인
    public void addOrderItemWithStockCheck(OrderItem orderItem) {
        // 재고 확인
        if (orderItem.getProduct().getStock() < orderItem.getQuantity()) {
            throw new IllegalStateException(
                "재고가 부족합니다. 상품: " + orderItem.getProduct().getName() + 
                ", 요청 수량: " + orderItem.getQuantity() + 
                ", 재고: " + orderItem.getProduct().getStock()
            );
        }
        
        // 주문 아이템 추가
        addOrderItem(orderItem);
        
        // 재고 차감
        orderItem.getProduct().decreaseStock(orderItem.getQuantity());
    }

    // 주문 취소 시 재고 복구
    public void restoreStock() {
        for (OrderItem orderItem : orderItems) {
            orderItem.getProduct().increaseStock(orderItem.getQuantity());
        }
    }

    // 주문 취소 가능 여부 확인
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    // 주문 취소
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("취소할 수 없는 주문 상태입니다: " + status);
        }
        changeStatus(OrderStatus.CANCELLED);
        restoreStock();
    }
}
