package com.example.commerce_mvp.domain.order;

import com.example.commerce_mvp.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 사용자별 주문 목록 조회 (페이징)
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);

    // 사용자별 주문 목록 조회 (전체)
    List<Order> findByUserOrderByOrderDateDesc(User user);

    // 주문 상태별 조회
    List<Order> findByStatus(OrderStatus status);

    // 특정 기간 주문 조회
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    // 사용자별 최근 주문 조회
    Optional<Order> findFirstByUserOrderByOrderDateDesc(User user);

    // 주문 상태 변경 가능한 주문들 조회
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.orderDate DESC")
    List<Order> findOrdersByStatusIn(@Param("statuses") List<OrderStatus> statuses);
}
