package com.example.commerce_mvp.application.order.dto;

import com.example.commerce_mvp.domain.order.Order;
import com.example.commerce_mvp.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long orderId;
    private String userEmail;
    private OrderStatus status;
    private int totalAmount;
    private String shippingAddress;
    private String shippingPhone;
    private String shippingName;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private List<OrderItemResponseDto> orderItems;

    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .userEmail(order.getUser().getEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .shippingPhone(order.getShippingPhone())
                .shippingName(order.getShippingName())
                .orderDate(order.getOrderDate())
                .deliveryDate(order.getDeliveryDate())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponseDto {
        private Long orderItemId;
        private Long productId;
        private String productName;
        private int quantity;
        private int price;
        private int totalPrice;

        public static OrderItemResponseDto from(com.example.commerce_mvp.domain.order.OrderItem orderItem) {
            return OrderItemResponseDto.builder()
                    .orderItemId(orderItem.getId())
                    .productId(orderItem.getProduct().getId())
                    .productName(orderItem.getProduct().getName())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .totalPrice(orderItem.getTotalPrice())
                    .build();
        }
    }
}
