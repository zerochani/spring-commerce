package com.example.commerce_mvp.domain.cart;

import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 사용자별 장바구니 조회 (페이징)
    Slice<Cart> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);

    // 사용자별 장바구니 전체 조회
    List<Cart> findByUserOrderByUpdatedAtDesc(User user);

    // 특정 사용자와 상품으로 장바구니 아이템 조회
    Optional<Cart> findByUserAndProduct(User user, Product product);

    // 동시성 제어를 위한 Lock 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.user = :user AND c.product = :product")
    Optional<Cart> findByUserAndProductWithLock(@Param("user") User user, @Param("product") Product product);

    // 사용자별 장바구니 아이템 수 조회
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user = :user")
    long countByUser(@Param("user") User user);

    // 사용자별 장바구니 총 금액 조회
    @Query("SELECT SUM(c.product.price * c.quantity) FROM Cart c WHERE c.user = :user")
    Long getTotalAmountByUser(@Param("user") User user);

    // 사용자별 장바구니 비우기
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user = :user")
    void deleteByUser(@Param("user") User user);

    // 특정 상품을 장바구니에서 제거
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user = :user AND c.product = :product")
    void deleteByUserAndProduct(@Param("user") User user, @Param("product") Product product);

    // 여러 상품을 장바구니에서 제거
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user = :user AND c.product IN :products")
    void deleteByUserAndProducts(@Param("user") User user, @Param("products") List<Product> products);

    // 재고 부족한 장바구니 아이템 조회
    @Query("SELECT c FROM Cart c WHERE c.user = :user AND c.quantity > c.product.stock")
    List<Cart> findOutOfStockItemsByUser(@Param("user") User user);
}
