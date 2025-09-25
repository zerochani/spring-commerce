package com.example.commerce_mvp.infrastructure.persistence;

import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.product.ProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long>, ProductRepository {
}
