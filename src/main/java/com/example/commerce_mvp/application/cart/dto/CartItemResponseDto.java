package com.example.commerce_mvp.application.cart.dto;

import com.example.commerce_mvp.domain.cart.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDto {
    private Long cartId;
    private Long productId;
    private String productName;
    private int productPrice;
    private String productImageUrl;
    private int quantity;
    private int totalPrice;
    private boolean isStockAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponseDto from(Cart cart) {
        return CartItemResponseDto.builder()
                .cartId(cart.getId())
                .productId(cart.getProduct().getId())
                .productName(cart.getProduct().getName())
                .productPrice(cart.getProduct().getPrice())
                .productImageUrl(cart.getProduct().getImageUrl())
                .quantity(cart.getQuantity())
                .totalPrice(cart.getTotalPrice())
                .isStockAvailable(cart.isStockAvailable())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
