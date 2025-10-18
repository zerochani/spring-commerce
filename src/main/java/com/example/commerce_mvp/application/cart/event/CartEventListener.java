package com.example.commerce_mvp.application.cart.event;

import com.example.commerce_mvp.application.cart.CartService;
import com.example.commerce_mvp.application.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventListener {

    private final CartService cartService;

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("주문 완료 이벤트 수신 - 주문 ID: {}, 사용자: {}, 총 금액: {}", 
                    event.getOrderId(), event.getUserEmail(), event.getTotalAmount());
            
            // 장바구니 비우기
            cartService.clearCartAfterOrder(event.getUserEmail());
            
            log.info("주문 완료 후 장바구니 비우기 성공 - 사용자: {}", event.getUserEmail());
            
        } catch (Exception e) {
            log.error("주문 완료 후 장바구니 비우기 실패 - 사용자: {}, 에러: {}", 
                    event.getUserEmail(), e.getMessage(), e);
            // 장바구니 비우기 실패해도 주문에는 영향 없음
        }
    }
}
