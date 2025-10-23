package com.example.commerce_mvp.application.order;

import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
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
import com.example.commerce_mvp.domain.user.UserRole;
import com.example.commerce_mvp.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("createOrder - 정상 요청이면 주문을 생성하고 이벤트를 발행한다")
    void createOrder_success() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);
        Product product = TestFixtures.createProduct(1L, "상품", 10000, 10);

        CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                .orderItems(List.of(CreateOrderRequestDto.OrderItemRequestDto.builder()
                        .productId(product.getId())
                        .quantity(2)
                        .build()))
                .shippingAddress("서울시 어딘가")
                .shippingName("홍길동")
                .shippingPhone("010-1234-5678")
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(productRepository.findByIdsWithLock(List.of(product.getId()))).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            TestFixtures.setId(order, 1L);
            long itemId = 1L;
            for (OrderItem item : order.getOrderItems()) {
                TestFixtures.setId(item, itemId++);
            }
            return order;
        });

        OrderResponseDto response = orderService.createOrder(userEmail, request);

        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualTo(20000);
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(product.getStock()).isEqualTo(8);

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OrderCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getOrderId()).isEqualTo(1L);
        assertThat(publishedEvent.getUserEmail()).isEqualTo(userEmail);
        assertThat(publishedEvent.getTotalAmount()).isEqualTo(20000);
    }

    @Test
    @DisplayName("createOrder - 존재하지 않는 상품 ID가 포함되면 예외가 발생한다")
    void createOrder_productNotFound() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);

        CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                .orderItems(List.of(CreateOrderRequestDto.OrderItemRequestDto.builder()
                        .productId(99L)
                        .quantity(1)
                        .build()))
                .shippingAddress("서울시")
                .shippingName("홍길동")
                .shippingPhone("010-1234-5678")
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(productRepository.findByIdsWithLock(List.of(99L))).thenReturn(Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(userEmail, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrder - 다른 사용자의 주문이면 접근이 거부된다")
    void getOrder_accessDeniedForDifferentUser() {
        User owner = TestFixtures.createUser("owner@example.com", UserRole.USER);
        User requester = TestFixtures.createUser("requester@example.com", UserRole.USER);
        setAuthentication(requester);

        Order order = Order.createOrder(owner, "주소", "010-0000-0000", "홍길동");
        TestFixtures.setId(order, 1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.getOrder(1L, requester.getEmail()));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("cancelOrder - 본인의 주문이면 주문을 취소하고 재고를 복구한다")
    void cancelOrder_success() {
        User user = TestFixtures.createUser("user@example.com", UserRole.USER);
        setAuthentication(user);

        Product product = TestFixtures.createProduct(1L, "상품", 10000, 10);

        Order order = Order.createOrder(user, "주소", "010-0000-0000", "홍길동");
        OrderItem orderItem = OrderItem.createOrderItem(product, 2);
        order.addOrderItemWithStockCheck(orderItem);
        order.calculateTotalAmount();
        TestFixtures.setId(order, 1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto response = orderService.cancelOrder(1L, user.getEmail());

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStock()).isEqualTo(10);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("updateOrderStatus - 일반 사용자는 주문 상태를 변경할 수 없다")
    void updateOrderStatus_requiresAdmin() {
        User user = TestFixtures.createUser("user@example.com", UserRole.USER);
        setAuthentication(user);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("updateOrderStatus - 관리자는 주문 상태를 변경할 수 있다")
    void updateOrderStatus_adminSuccess() {
        User admin = TestFixtures.createUser("admin@example.com", UserRole.ADMIN);
        setAuthentication(admin);

        Order order = Order.createOrder(admin, "주소", "010-0000-0000", "관리자");
        TestFixtures.setId(order, 1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("getMyOrders - 사용자 주문 목록을 페이지 단위로 조회한다")
    void getMyOrders_success() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);
        Product product = TestFixtures.createProduct(1L, "상품", 10000, 10);

        Order order = Order.createOrder(user, "주소", "010-0000-0000", "홍길동");
        order.addOrderItemWithStockCheck(OrderItem.createOrderItem(product, 1));
        order.calculateTotalAmount();
        TestFixtures.setId(order, 1L);

        PageRequest pageRequest = PageRequest.of(0, 10);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByOrderDateDesc(user, pageRequest))
                .thenReturn(new PageImpl<>(List.of(order), pageRequest, 1));

        SliceResponse<OrderResponseDto> response = orderService.getMyOrders(userEmail, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getContent().get(0).getOrderId()).isEqualTo(1L);
    }

    private void setAuthentication(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority(user.getRole().getValue()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
