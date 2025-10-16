package com.example.commerce_mvp.presentation.cart;

import com.example.commerce_mvp.application.cart.CartService;
import com.example.commerce_mvp.application.cart.dto.AddCartItemRequestDto;
import com.example.commerce_mvp.application.cart.dto.CartItemResponseDto;
import com.example.commerce_mvp.application.cart.dto.CartSummaryResponseDto;
import com.example.commerce_mvp.application.cart.dto.CreateOrderFromCartRequestDto;
import com.example.commerce_mvp.application.cart.dto.UpdateCartItemRequestDto;
import com.example.commerce_mvp.application.order.dto.OrderResponseDto;
import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.common.util.AuthorizationUtils;
import com.example.commerce_mvp.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDto> addCartItem(
            @Valid @RequestBody AddCartItemRequestDto request,
            Authentication authentication) {
        
        User currentUser = AuthorizationUtils.getCurrentUser();
        CartItemResponseDto response = cartService.addCartItem(currentUser.getEmail(), request);
        
        log.info("장바구니 아이템 추가 API 호출 - 사용자: {}, 상품 ID: {}", currentUser.getEmail(), request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/items")
    public ResponseEntity<SliceResponse<CartItemResponseDto>> getCartItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        User currentUser = AuthorizationUtils.getCurrentUser();
        SliceResponse<CartItemResponseDto> response = cartService.getCartItems(currentUser.getEmail(), page, size);
        
        log.info("장바구니 조회 API 호출 - 사용자: {}, 페이지: {}, 크기: {}", currentUser.getEmail(), page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<CartSummaryResponseDto> getCartSummary(Authentication authentication) {
        User currentUser = AuthorizationUtils.getCurrentUser();
        CartSummaryResponseDto response = cartService.getCartSummary(currentUser.getEmail());
        
        log.info("장바구니 요약 조회 API 호출 - 사용자: {}", currentUser.getEmail());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{cartId}")
    public ResponseEntity<CartItemResponseDto> updateCartItem(
            @PathVariable Long cartId,
            @Valid @RequestBody UpdateCartItemRequestDto request,
            Authentication authentication) {
        
        User currentUser = AuthorizationUtils.getCurrentUser();
        CartItemResponseDto response = cartService.updateCartItem(cartId, currentUser.getEmail(), request);
        
        log.info("장바구니 아이템 수정 API 호출 - 사용자: {}, 장바구니 ID: {}", currentUser.getEmail(), cartId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long cartId,
            Authentication authentication) {
        
        User currentUser = AuthorizationUtils.getCurrentUser();
        cartService.removeCartItem(cartId, currentUser.getEmail());
        
        log.info("장바구니 아이템 삭제 API 호출 - 사용자: {}, 장바구니 ID: {}", currentUser.getEmail(), cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        User currentUser = AuthorizationUtils.getCurrentUser();
        cartService.clearCart(currentUser.getEmail());
        
        log.info("장바구니 비우기 API 호출 - 사용자: {}", currentUser.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/out-of-stock")
    public ResponseEntity<Void> removeOutOfStockItems(Authentication authentication) {
        User currentUser = AuthorizationUtils.getCurrentUser();
        cartService.removeOutOfStockItems(currentUser.getEmail());
        
        log.info("재고 부족 아이템 제거 API 호출 - 사용자: {}", currentUser.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/order")
    public ResponseEntity<OrderResponseDto> createOrderFromCart(
            @Valid @RequestBody CreateOrderFromCartRequestDto request,
            Authentication authentication) {
        
        User currentUser = AuthorizationUtils.getCurrentUser();
        OrderResponseDto response = cartService.createOrderFromCart(currentUser.getEmail(), request);
        
        log.info("장바구니에서 주문 생성 API 호출 - 사용자: {}, 주문 ID: {}", currentUser.getEmail(), response.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
