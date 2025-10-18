package com.example.commerce_mvp.application.order;

import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.application.common.util.AuthorizationUtils;
import com.example.commerce_mvp.application.order.dto.CreateOrderRequestDto;
import com.example.commerce_mvp.application.order.dto.OrderResponseDto;
import com.example.commerce_mvp.application.order.event.OrderCreatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponseDto createOrder(String userEmail, CreateOrderRequestDto request) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 주문 생성 (도메인 팩토리 메서드 사용)
        Order order = Order.createOrder(user, request.getShippingAddress(), 
                request.getShippingPhone(), request.getShippingName());

        // 상품 ID 목록 추출
        List<Long> productIds = request.getOrderItems().stream()
                .map(CreateOrderRequestDto.OrderItemRequestDto::getProductId)
                .collect(Collectors.toList());

        // 동시성 제어를 위해 상품들을 Lock으로 조회
        List<Product> products = productRepository.findByIdsWithLock(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        // 주문 아이템 생성 및 재고 확인 (도메인 로직 사용)
        for (CreateOrderRequestDto.OrderItemRequestDto itemRequest : request.getOrderItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다: " + itemRequest.getProductId());
            }

            // OrderItem 생성 (도메인 팩토리 메서드 사용)
            OrderItem orderItem = OrderItem.createOrderItem(product, itemRequest.getQuantity());
            
            // 주문 아이템 추가 및 재고 확인/차감 (도메인 로직 사용)
            order.addOrderItemWithStockCheck(orderItem);
        }

        // 총 금액 계산
        order.calculateTotalAmount();

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 주문 생성 완료 이벤트 발행
        eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder.getId(), userEmail, savedOrder.getTotalAmount()));

        log.info("주문 생성 완료 - 주문 ID: {}, 사용자: {}, 총 금액: {}", 
                savedOrder.getId(), userEmail, savedOrder.getTotalAmount());

        return OrderResponseDto.from(savedOrder);
    }

    public OrderResponseDto getOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));

        // 권한 확인 (도메인 로직 사용)
        AuthorizationUtils.validateUserOwnership(userEmail);
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

        // 권한 확인
        AuthorizationUtils.validateUserOwnership(userEmail);
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 주문만 취소할 수 있습니다.");
        }

        // 주문 취소 (도메인 로직 사용)
        order.cancel();
        
        // 변경사항 저장
        Order savedOrder = orderRepository.save(order);

        log.info("주문 취소 완료 - 주문 ID: {}, 사용자: {}", orderId, userEmail);

        return OrderResponseDto.from(savedOrder);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // 관리자 권한 확인
        AuthorizationUtils.validateAdminRole();
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));

        order.changeStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        log.info("주문 상태 변경 완료 - 주문 ID: {}, 상태: {}", orderId, newStatus);

        return OrderResponseDto.from(savedOrder);
    }
}
