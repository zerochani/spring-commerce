package com.example.commerce_mvp.domain.product;

import java.util.List;

public interface ProductRepository {
    List<Product> saveAll(List<Product> products);
    boolean existsByNaverProductId(String naverProductId);
    List<Product> findAll();
}
