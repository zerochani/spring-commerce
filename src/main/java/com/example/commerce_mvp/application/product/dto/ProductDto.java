package com.example.commerce_mvp.application.product.dto;

import com.example.commerce_mvp.domain.product.Product;
import lombok.Getter;

@Getter
public class ProductDto {

    private final Long id;
    private final String name;
    private final int price;

    private ProductDto(Long id, String name, int price){
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static ProductDto from(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getPrice());
    }
}
