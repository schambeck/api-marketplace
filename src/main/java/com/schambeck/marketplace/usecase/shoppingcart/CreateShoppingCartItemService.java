package com.schambeck.marketplace.usecase.shoppingcart;

import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import com.schambeck.marketplace.usecase.cartitem.CreateShoppingCartItemRequest;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
import com.schambeck.marketplace.usecase.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateShoppingCartItemService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItem create(CreateShoppingCartItemRequest request) {
        Product product = productRepository.findByIdOrThrow(request.getProductId());
        BigDecimal total = calculateTotal(product.getPrice(), request.getQuantity());
        Cart cart = createCart(request);
        cart.setTotal(cart.getTotal().add(total));
        CartItem cartItem = createCartItem(request, cart, product, total);
        return cartItemRepository.save(cartItem);
    }
    
    private static CartItem createCartItem(CreateShoppingCartItemRequest request, Cart cart, Product product, BigDecimal total) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .price(product.getPrice())
                .quantity(request.getQuantity())
                .total(total)
                .version(0)
                .build();
    }
    
    private Cart createCart(CreateShoppingCartItemRequest request) {
        return cartRepository.findByUser_Username(request.getUsername())
                .orElse(Cart.builder()
                        .user(userRepository.findByUsernameOrThrow(request.getUsername()))
                        .total(ZERO)
                        .version(0)
                        .build());
    }
    
    private BigDecimal calculateTotal(BigDecimal price, BigDecimal quantity) {
        return price.multiply(quantity).setScale(2, HALF_UP);
    }
}
