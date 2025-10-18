package com.example.commerce_mvp.application.order.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final String userEmail;
    private final int totalAmount;
    
    public OrderCreatedEvent(Object source, Long orderId, String userEmail, int totalAmount) {
        super(source);
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.totalAmount = totalAmount;
    }
}
