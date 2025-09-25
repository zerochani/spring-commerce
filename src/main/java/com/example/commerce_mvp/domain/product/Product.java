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

    //이 매개변수들로 객체를 만든다
    public static Product of(String name, int price, String imageUrl, String naverProductId){
        Product product = new Product();
        product.name = name;
        product.price = price;
        product.imageUrl = imageUrl;
        product.naverProductId = naverProductId;
        product.stock = 100;
        return product;
    }
}
