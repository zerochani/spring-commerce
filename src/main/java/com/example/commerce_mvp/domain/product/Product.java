package com.example.commerce_mvp.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
