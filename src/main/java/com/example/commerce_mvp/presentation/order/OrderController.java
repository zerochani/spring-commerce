package com.example.commerce_mvp.presentation.order;

import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.order.OrderService;
import com.example.commerce_mvp.application.order.dto.CreateOrderRequestDto;
import com.example.commerce_mvp.application.order.dto.OrderResponseDto;
import com.example.commerce_mvp.domain.order.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        OrderResponseDto response = orderService.createOrder(userEmail, request);
        
        log.info("주문 생성 API 호출 - 사용자: {}", userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        OrderResponseDto response = orderService.getOrder(orderId, userEmail);
        
        log.info("주문 조회 API 호출 - 주문 ID: {}, 사용자: {}", orderId, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SliceResponse<OrderResponseDto>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        SliceResponse<OrderResponseDto> response = orderService.getMyOrders(userEmail, page, size);
        
        log.info("내 주문 목록 조회 API 호출 - 사용자: {}, 페이지: {}, 크기: {}", userEmail, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        OrderResponseDto response = orderService.cancelOrder(orderId, userEmail);
        
        log.info("주문 취소 API 호출 - 주문 ID: {}, 사용자: {}", orderId, userEmail);
        return ResponseEntity.ok(response);
    }

    // 관리자용 - 주문 상태 변경
    @PostMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        OrderResponseDto response = orderService.updateOrderStatus(orderId, status);
        
        log.info("주문 상태 변경 API 호출 - 주문 ID: {}, 상태: {}, 관리자: {}", orderId, status, userEmail);
        return ResponseEntity.ok(response);
    }
}
