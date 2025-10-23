package com.example.commerce_mvp.application.cart;

import com.example.commerce_mvp.application.cart.dto.AddCartItemRequestDto;
import com.example.commerce_mvp.application.cart.dto.CartItemResponseDto;
import com.example.commerce_mvp.application.cart.dto.CartSummaryResponseDto;
import com.example.commerce_mvp.application.cart.dto.CreateOrderFromCartRequestDto;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.application.order.OrderService;
import com.example.commerce_mvp.application.order.dto.CreateOrderRequestDto;
import com.example.commerce_mvp.application.order.dto.OrderResponseDto;
import com.example.commerce_mvp.application.common.util.SecurityContextUtils;
import com.example.commerce_mvp.domain.cart.Cart;
import com.example.commerce_mvp.domain.cart.CartRepository;
import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.product.ProductRepository;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import com.example.commerce_mvp.domain.user.UserRole;
import com.example.commerce_mvp.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderService orderService;

    @Test
    @DisplayName("addCartItem - 기존 장바구니 아이템이 있으면 수량을 증가시킨다")
    void addCartItem_existingCart() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);
        Product product = TestFixtures.createProduct(1L, "상품", 10000, 10);
        Cart existingCart = TestFixtures.createCart(1L, user, product, 1);

        AddCartItemRequestDto request = AddCartItemRequestDto.builder()
                .productId(product.getId())
                .quantity(2)
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProductWithLock(user, product)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(existingCart)).thenReturn(existingCart);

        try (MockedStatic<SecurityContextUtils> securityMock = mockStatic(SecurityContextUtils.class)) {
            securityMock.when(SecurityContextUtils::getCurrentUserEmail).thenReturn(userEmail);

            CartItemResponseDto response = cartService.addCartItem(request);

            assertThat(response.getQuantity()).isEqualTo(3);
            verify(cartRepository).save(existingCart);
        }
    }

    @Test
    @DisplayName("addCartItem - 새로운 상품은 장바구니에 추가한다")
    void addCartItem_newCart() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);
        Product product = TestFixtures.createProduct(1L, "상품", 10000, 10);

        AddCartItemRequestDto request = AddCartItemRequestDto.builder()
                .productId(product.getId())
                .quantity(2)
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProductWithLock(user, product)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            TestFixtures.setId(cart, 10L);
            return cart;
        });

        try (MockedStatic<SecurityContextUtils> securityMock = mockStatic(SecurityContextUtils.class)) {
            securityMock.when(SecurityContextUtils::getCurrentUserEmail).thenReturn(userEmail);

            CartItemResponseDto response = cartService.addCartItem(request);

            assertThat(response.getCartId()).isEqualTo(10L);
            assertThat(response.getQuantity()).isEqualTo(2);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Test
    @DisplayName("createOrderFromCartItems - 장바구니 상품으로 주문을 생성한다")
    void createOrderFromCartItems_success() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);
        Product product1 = TestFixtures.createProduct(1L, "상품1", 10000, 10);
        Product product2 = TestFixtures.createProduct(2L, "상품2", 15000, 5);
        Cart cart1 = TestFixtures.createCart(1L, user, product1, 1);
        Cart cart2 = TestFixtures.createCart(2L, user, product2, 2);

        CreateOrderFromCartRequestDto request = CreateOrderFromCartRequestDto.builder()
                .shippingAddress("서울시")
                .shippingName("홍길동")
                .shippingPhone("010-0000-0000")
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserOrderByUpdatedAtDesc(user)).thenReturn(List.of(cart1, cart2));
        when(cartRepository.findOutOfStockItemsByUser(user)).thenReturn(List.of());

        OrderResponseDto expectedResponse = OrderResponseDto.builder()
                .orderId(99L)
                .userEmail(userEmail)
                .totalAmount(40000)
                .build();

        when(orderService.createOrder(eq(userEmail), any(CreateOrderRequestDto.class))).thenReturn(expectedResponse);

        try (MockedStatic<SecurityContextUtils> securityMock = mockStatic(SecurityContextUtils.class)) {
            securityMock.when(SecurityContextUtils::getCurrentUserEmail).thenReturn(userEmail);

            OrderResponseDto response = cartService.createOrderFromCartItems(request);

            assertThat(response.getOrderId()).isEqualTo(99L);

            ArgumentCaptor<CreateOrderRequestDto> orderRequestCaptor = ArgumentCaptor.forClass(CreateOrderRequestDto.class);
            verify(orderService).createOrder(eq(userEmail), orderRequestCaptor.capture());

            CreateOrderRequestDto capturedRequest = orderRequestCaptor.getValue();
            assertThat(capturedRequest.getOrderItems()).hasSize(2);
            assertThat(capturedRequest.getOrderItems().get(0).getProductId()).isEqualTo(product1.getId());
            assertThat(capturedRequest.getOrderItems().get(1).getQuantity()).isEqualTo(cart2.getQuantity());
        }
    }

    @Test
    @DisplayName("createOrderFromCartItems - 장바구니가 비어있으면 예외가 발생한다")
    void createOrderFromCartItems_emptyCart() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserOrderByUpdatedAtDesc(user)).thenReturn(List.of());

        CreateOrderFromCartRequestDto request = CreateOrderFromCartRequestDto.builder()
                .shippingAddress("서울시")
                .shippingName("홍길동")
                .shippingPhone("010-0000-0000")
                .build();

        try (MockedStatic<SecurityContextUtils> securityMock = mockStatic(SecurityContextUtils.class)) {
            securityMock.when(SecurityContextUtils::getCurrentUserEmail).thenReturn(userEmail);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> cartService.createOrderFromCartItems(request));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("createOrderFromCartItems - 재고 부족 상품이 있으면 예외가 발생한다")
    void createOrderFromCartItems_outOfStockItems() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);
        Product product = TestFixtures.createProduct(1L, "상품", 10000, 0);
        Cart cart = TestFixtures.createCart(1L, user, product, 1);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserOrderByUpdatedAtDesc(user)).thenReturn(List.of(cart));
        when(cartRepository.findOutOfStockItemsByUser(user)).thenReturn(List.of(cart));

        CreateOrderFromCartRequestDto request = CreateOrderFromCartRequestDto.builder()
                .shippingAddress("서울시")
                .shippingName("홍길동")
                .shippingPhone("010-0000-0000")
                .build();

        try (MockedStatic<SecurityContextUtils> securityMock = mockStatic(SecurityContextUtils.class)) {
            securityMock.when(SecurityContextUtils::getCurrentUserEmail).thenReturn(userEmail);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> cartService.createOrderFromCartItems(request));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_OUT_OF_STOCK);
        }
    }

    @Test
    @DisplayName("getCartSummary - 장바구니 요약 정보를 반환한다")
    void getCartSummary_success() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);
        Product product = TestFixtures.createProduct(1L, "상품", 10000, 10);
        Cart cart = TestFixtures.createCart(1L, user, product, 2);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserOrderByUpdatedAtDesc(user)).thenReturn(List.of(cart));
        when(cartRepository.findOutOfStockItemsByUser(user)).thenReturn(List.of(cart));
        when(cartRepository.getTotalAmountByUser(user)).thenReturn(20000L);

        try (MockedStatic<SecurityContextUtils> securityMock = mockStatic(SecurityContextUtils.class)) {
            securityMock.when(SecurityContextUtils::getCurrentUserEmail).thenReturn(userEmail);

            CartSummaryResponseDto summary = cartService.getCartSummary();

            assertThat(summary.getTotalItems()).isEqualTo(1);
            assertThat(summary.getTotalAmount()).isEqualTo(20000);
            assertThat(summary.isHasOutOfStockItems()).isTrue();
            assertThat(summary.getCartItems()).hasSize(1);
        }
    }

    @Test
    @DisplayName("clearCartAfterOrder - 주문 완료 후 장바구니를 비운다")
    void clearCartAfterOrder_success() {
        String userEmail = "user@example.com";
        User user = TestFixtures.createUser(userEmail, UserRole.USER);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        cartService.clearCartAfterOrder(userEmail);

        verify(cartRepository).deleteByUser(user);
    }
}
