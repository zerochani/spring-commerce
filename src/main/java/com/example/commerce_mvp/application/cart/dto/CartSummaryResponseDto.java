package com.example.commerce_mvp.application.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSummaryResponseDto {
    private int totalItems;
    private int totalAmount;
    private List<CartItemResponseDto> cartItems;
    private boolean hasOutOfStockItems;

    public static CartSummaryResponseDto of(List<CartItemResponseDto> cartItems, int totalAmount, boolean hasOutOfStockItems) {
        return CartSummaryResponseDto.builder()
                .totalItems(cartItems.size())
                .totalAmount(totalAmount)
                .cartItems(cartItems)
                .hasOutOfStockItems(hasOutOfStockItems)
                .build();
    }
}
