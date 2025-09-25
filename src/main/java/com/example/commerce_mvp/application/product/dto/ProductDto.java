package com.example.commerce_mvp.application.product.dto;

import com.example.commerce_mvp.domain.product.Product;
import lombok.Getter;

@Getter
public class ProductDto {

    private final Long productId;
    private final String name;
    private final String imageUrl;
    private final int price;
    private final String category1;
    private final String category2;

    public ProductDto(Product product){
        this.productId=product.getId();
        this.name = product.getName();
        this.imageUrl = product.getImageUrl();
        this.price = product.getPrice();
        this.category1 = product.getCategory1();
        this.category2 = product.getCategory2();
    }
}
