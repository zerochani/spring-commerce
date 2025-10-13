package com.example.commerce_mvp.domain.order;

import com.example.commerce_mvp.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int price; // 주문 시점의 가격

    @Builder
    public OrderItem(Product product, int quantity, int price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    // Order와의 양방향 관계 설정
    public void setOrder(Order order) {
        this.order = order;
    }

    // 총 가격 계산
    public int getTotalPrice() {
        return this.price * this.quantity;
    }

    // 수량 변경
    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }

    // OrderItem 생성 팩토리 메서드
    public static OrderItem createOrderItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .build();
    }
}
