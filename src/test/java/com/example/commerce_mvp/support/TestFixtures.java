package com.example.commerce_mvp.support;

import com.example.commerce_mvp.domain.cart.Cart;
import com.example.commerce_mvp.domain.product.Product;
import com.example.commerce_mvp.domain.user.SocialProvider;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRole;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User createUser(String email, UserRole role) {
        return User.builder()
                .email(email)
                .username("user-" + role.name().toLowerCase())
                .provider(SocialProvider.GOOGLE)
                .providerId("provider-" + email)
                .role(role)
                .build();
    }

    public static Product createProduct(Long id, String name, int price, int stock) {
        Product product = Product.of(
                name,
                price,
                "https://example.com/" + name,
                "naver-" + id,
                "category1",
                "category2"
        );
        setId(product, id);
        ReflectionTestUtils.setField(product, "stock", stock);
        return product;
    }

    public static Cart createCart(Long id, User user, Product product, int quantity) {
        Cart cart = Cart.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build();
        setId(cart, id);
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(cart, "createdAt", now.minusDays(1));
        ReflectionTestUtils.setField(cart, "updatedAt", now);
        return cart;
    }

    public static void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
