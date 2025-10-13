package com.example.commerce_mvp.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Lock;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int price;
    private int stock;
    private String imageUrl;
    @Column(unique = true)
    private String naverProductId;

    private String category1;
    private String category2;

    //이 매개변수들로 객체를 만든다
    public static Product of(String name, int price, String imageUrl, String naverProductId, String category1, String category2){
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.imageUrl = imageUrl;
        product.naverProductId = naverProductId;
        product.stock = 100;
        product.category1 = category1;
        product.category2 = category2;
        return product;
    }

    // 재고 업데이트
    public void updateStock(int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("재고는 0보다 작을 수 없습니다.");
        }
        this.stock = newStock;
    }

    // 재고 차감 (동시성 제어)
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stock + ", 요청 수량: " + quantity);
        }
        this.stock -= quantity;
    }

    // 재고 증가
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
