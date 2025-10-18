package com.example.commerce_mvp.application.cart;

import com.example.commerce_mvp.application.common.dto.SliceResponse;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.application.common.util.AuthorizationUtils;
import com.example.commerce_mvp.application.cart.dto.AddCartItemRequestDto;
import com.example.commerce_mvp.application.cart.dto.CartItemResponseDto;
import com.example.commerce_mvp.application.cart.dto.CartSummaryResponseDto;
import com.example.commerce_mvp.application.cart.dto.CreateOrderFromCartRequestDto;
import com.example.commerce_mvp.application.cart.dto.UpdateCartItemRequestDto;
import com.example.commerce_mvp.application.order.OrderService;
import com.example.commerce_mvp.application.order.dto.CreateOrderRequestDto;
import com.example.commerce_mvp.application.order.dto.OrderResponseDto;
import com.example.commerce_mvp.domain.cart.Cart;
import com.example.commerce_mvp.domain.cart.CartRepository;
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
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    @Transactional
    public CartItemResponseDto addCartItem(String userEmail, AddCartItemRequestDto request) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다: " + request.getProductId()));

        // 동시성 제어를 위해 Lock으로 기존 장바구니 아이템 조회
        Cart existingCart = cartRepository.findByUserAndProductWithLock(user, product).orElse(null);

        Cart cart;
        if (existingCart != null) {
            // 기존 아이템이 있으면 수량 증가
            existingCart.increaseQuantity(request.getQuantity());
            cart = cartRepository.save(existingCart);
            log.info("장바구니 수량 증가 - 사용자: {}, 상품: {}, 수량: {}", userEmail, product.getName(), request.getQuantity());
        } else {
            // 새 아이템 추가
            cart = Cart.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart = cartRepository.save(cart);
            log.info("장바구니 아이템 추가 - 사용자: {}, 상품: {}, 수량: {}", userEmail, product.getName(), request.getQuantity());
        }

        return CartItemResponseDto.from(cart);
    }

    public SliceResponse<CartItemResponseDto> getCartItems(String userEmail, int page, int size) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        PageRequest pageRequest = PageRequest.of(page, size);
        Slice<Cart> cartSlice = cartRepository.findByUserOrderByUpdatedAtDesc(user, pageRequest);

        List<CartItemResponseDto> content = cartSlice.getContent().stream()
                .map(CartItemResponseDto::from)
                .collect(Collectors.toList());

        return new SliceResponse<>(content, cartSlice.hasNext(), null);
    }

    public CartSummaryResponseDto getCartSummary(String userEmail) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 장바구니 아이템 조회
        List<Cart> cartItems = cartRepository.findByUserOrderByUpdatedAtDesc(user);
        
        // 재고 부족 아이템 확인
        List<Cart> outOfStockItems = cartRepository.findOutOfStockItemsByUser(user);
        boolean hasOutOfStockItems = !outOfStockItems.isEmpty();

        // 총 금액 계산
        Long totalAmount = cartRepository.getTotalAmountByUser(user);
        if (totalAmount == null) {
            totalAmount = 0L;
        }

        List<CartItemResponseDto> cartItemDtos = cartItems.stream()
                .map(CartItemResponseDto::from)
                .collect(Collectors.toList());

        return CartSummaryResponseDto.of(cartItemDtos, totalAmount.intValue(), hasOutOfStockItems);
    }

    @Transactional
    public CartItemResponseDto updateCartItem(Long cartItemId, String userEmail, UpdateCartItemRequestDto request) {
        // 장바구니 아이템 조회 (@PreAuthorize에서 권한 검증)
        Cart cart = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "장바구니 아이템을 찾을 수 없습니다: " + cartItemId));

        // 수량 업데이트
        cart.updateQuantity(request.getQuantity());
        Cart savedCart = cartRepository.save(cart);

        log.info("장바구니 아이템 수정 - 사용자: {}, 상품: {}, 수량: {}", userEmail, cart.getProduct().getName(), request.getQuantity());

        return CartItemResponseDto.from(savedCart);
    }

    @Transactional
    public void removeCartItem(Long cartItemId, String userEmail) {
        // 장바구니 아이템 조회 (@PreAuthorize에서 권한 검증)
        Cart cart = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "장바구니 아이템을 찾을 수 없습니다: " + cartItemId));

        cartRepository.delete(cart);

        log.info("장바구니 아이템 삭제 - 사용자: {}, 상품: {}", userEmail, cart.getProduct().getName());
    }

    @Transactional
    public void clearCart(String userEmail) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        cartRepository.deleteByUser(user);

        log.info("장바구니 비우기 - 사용자: {}", userEmail);
    }

    @Transactional
    public void removeOutOfStockItems(String userEmail) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 재고 부족 아이템 조회
        List<Cart> outOfStockItems = cartRepository.findOutOfStockItemsByUser(user);
        
        if (!outOfStockItems.isEmpty()) {
            cartRepository.deleteAll(outOfStockItems);
            log.info("재고 부족 아이템 제거 - 사용자: {}, 제거된 아이템 수: {}", userEmail, outOfStockItems.size());
        }
    }

    @Transactional
    public OrderResponseDto createOrderFromCartItems(String userEmail, CreateOrderFromCartRequestDto request) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 장바구니 아이템 조회
        List<Cart> cartItems = cartRepository.findByUserOrderByUpdatedAtDesc(user);
        
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "장바구니가 비어있습니다.");
        }

        // 재고 부족 아이템 확인
        List<Cart> outOfStockItems = cartRepository.findOutOfStockItemsByUser(user);
        if (!outOfStockItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_ITEM_OUT_OF_STOCK, "재고가 부족한 상품이 있습니다. 장바구니를 확인해주세요.");
        }

        // 주문 요청 DTO 생성
        List<CreateOrderRequestDto.OrderItemRequestDto> orderItems = cartItems.stream()
                .map(cart -> CreateOrderRequestDto.OrderItemRequestDto.builder()
                        .productId(cart.getProduct().getId())
                        .quantity(cart.getQuantity())
                        .build())
                .collect(Collectors.toList());

        CreateOrderRequestDto orderRequest = CreateOrderRequestDto.builder()
                .orderItems(orderItems)
                .shippingAddress(request.getShippingAddress())
                .shippingPhone(request.getShippingPhone())
                .shippingName(request.getShippingName())
                .build();

        // 주문 생성만 담당 (장바구니 비우기는 별도 처리)
        OrderResponseDto orderResponse = orderService.createOrder(userEmail, orderRequest);

        log.info("장바구니에서 주문 생성 완료 - 사용자: {}, 주문 ID: {}", userEmail, orderResponse.getOrderId());

        return orderResponse;
    }

    @Transactional
    public void clearCartAfterOrder(String userEmail) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userEmail));

        // 장바구니 비우기
        cartRepository.deleteByUser(user);

        log.info("주문 후 장바구니 비우기 완료 - 사용자: {}", userEmail);
    }

    // @PreAuthorize에서 사용할 장바구니 아이템 소유자 이메일 조회
    public String getCartItemOwnerEmail(Long cartItemId) {
        Cart cart = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        return cart.getUser().getEmail();
    }
}
