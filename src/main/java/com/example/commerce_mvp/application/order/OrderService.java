package com.example.commerce_mvp.application.order;

import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.application.order.dto.CreateOrderRequestDto;
import com.example.commerce_mvp.application.order.dto.OrderResponseDto;
import com.example.commerce_mvp.domain.order.Order;
import com.example.commerce_mvp.domain.order.OrderItem;
import com.example.commerce_mvp.domain.order.OrderRepository;
import com.example.commerce_mvp.domain.order.OrderStatus;
import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.product.ProductRepository;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponseDto createOrder(String userEmail, CreateOrderRequestDto request) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 주문 생성
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(0) // 나중에 계산
                .shippingAddress(request.getShippingAddress())
                .shippingPhone(request.getShippingPhone())
                .shippingName(request.getShippingName())
                .build();

        // 주문 아이템 생성 및 재고 확인
        for (CreateOrderRequestDto.OrderItemRequestDto itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다: " + itemRequest.getProductId()));

            // 재고 확인
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, 
                    "재고가 부족합니다. 상품: " + product.getName() + ", 요청 수량: " + itemRequest.getQuantity() + ", 재고: " + product.getStock());
            }

            // 주문 아이템 생성
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);
        }

        // 총 금액 계산
        order.calculateTotalAmount();

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 재고 차감
        for (OrderItem orderItem : savedOrder.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.updateStock(product.getStock() - orderItem.getQuantity());
            productRepository.save(product);
        }

        log.info("주문 생성 완료 - 주문 ID: {}, 사용자: {}, 총 금액: {}", 
                savedOrder.getId(), userEmail, savedOrder.getTotalAmount());

        return OrderResponseDto.from(savedOrder);
    }

    public OrderResponseDto getOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));

        // 본인 주문만 조회 가능
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 주문만 조회할 수 있습니다.");
        }

        return OrderResponseDto.from(order);
    }

    public SliceResponse<OrderResponseDto> getMyOrders(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        PageRequest pageRequest = PageRequest.of(page, size);
        Slice<Order> orderSlice = orderRepository.findByUserOrderByOrderDateDesc(user, pageRequest);

        List<OrderResponseDto> content = orderSlice.getContent().stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());

        return new SliceResponse<>(content, orderSlice.hasNext(), null);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));

        // 본인 주문만 취소 가능
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 주문만 취소할 수 있습니다.");
        }

        // 주문 상태 확인
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, "취소할 수 없는 주문 상태입니다: " + order.getStatus());
        }

        // 주문 취소
        order.changeStatus(OrderStatus.CANCELLED);

        // 재고 복구
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.updateStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }

        log.info("주문 취소 완료 - 주문 ID: {}, 사용자: {}", orderId, userEmail);

        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));

        order.changeStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        log.info("주문 상태 변경 완료 - 주문 ID: {}, 상태: {}", orderId, newStatus);

        return OrderResponseDto.from(savedOrder);
    }
}
