package com.example.commerce_mvp.application.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {

    @NotEmpty(message = "주문 상품 목록은 필수입니다.")
    @Valid
    private List<OrderItemRequestDto> orderItems;

    @NotBlank(message = "배송지 주소는 필수입니다.")
    private String shippingAddress;

    @NotBlank(message = "배송지 전화번호는 필수입니다.")
    private String shippingPhone;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String shippingName;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequestDto {
        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;

        @NotNull(message = "주문 수량은 필수입니다.")
        private Integer quantity;
    }
}
