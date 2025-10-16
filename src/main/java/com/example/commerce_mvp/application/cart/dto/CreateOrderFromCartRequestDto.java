package com.example.commerce_mvp.application.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderFromCartRequestDto {

    @NotBlank(message = "배송지 주소는 필수입니다.")
    private String shippingAddress;

    @NotBlank(message = "배송지 전화번호는 필수입니다.")
    private String shippingPhone;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String shippingName;
}
