package com.example.commerce_mvp.domain.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNaverProductId(String naverProductId);

    @Query("SELECT p FROM Product p WHERE p.id > :cursorId ORDER BY p.id ASC")
    Slice<Product> findProductsAfterCursor(@Param("cursorId") Long cursorId, Pageable pageable);

    Slice<Product> findAllByOrderByIdAsc(Pageable pageable);
}
