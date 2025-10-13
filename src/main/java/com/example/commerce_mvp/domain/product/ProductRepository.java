package com.example.commerce_mvp.domain.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNaverProductId(String naverProductId);

    @Query("SELECT p FROM Product p WHERE p.id > :cursorId ORDER BY p.id ASC")
    Slice<Product> findProductsAfterCursor(@Param("cursorId") Long cursorId, Pageable pageable);

    Slice<Product> findAllByOrderByIdAsc(Pageable pageable);

    // 동시성 제어를 위한 Pessimistic Lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    // 여러 상품을 동시에 Lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findByIdsWithLock(@Param("ids") List<Long> ids);
}
