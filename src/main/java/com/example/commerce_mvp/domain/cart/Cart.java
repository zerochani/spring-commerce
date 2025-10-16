package com.example.commerce_mvp.domain.cart;

import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Cart(User user, Product product, int quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 수량 변경
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        this.quantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    // 수량 증가
    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가할 수량은 0보다 커야 합니다.");
        }
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    // 수량 감소
    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소할 수량은 0보다 커야 합니다.");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("감소할 수량이 현재 수량보다 큽니다.");
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    // 총 가격 계산
    public int getTotalPrice() {
        return this.product.getPrice() * this.quantity;
    }

    // 재고 확인
    public boolean isStockAvailable() {
        return this.product.getStock() >= this.quantity;
    }

    // 재고 부족 시 예외 발생
    public void validateStock() {
        if (!isStockAvailable()) {
            throw new IllegalStateException(
                "재고가 부족합니다. 상품: " + this.product.getName() + 
                ", 요청 수량: " + this.quantity + 
                ", 재고: " + this.product.getStock()
            );
        }
    }
}
