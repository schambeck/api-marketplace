package com.schambeck.marketplace.usecase.shoppingcart;

import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import com.schambeck.marketplace.usecase.cartitem.UpdateShoppingCartItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateShoppingCartItemService {
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItem updateQuantity(Integer cartItemId, UpdateShoppingCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findByIdOrThrow(cartItemId);
        BigDecimal total = calculateTotal(cartItem.getPrice(), request.getQuantity());
        Cart cart = cartItem.getCart();
        cart.setTotal(cart.getTotal().subtract(cartItem.getTotal()).add(total));
        cartItem.setQuantity(request.getQuantity());
        cartItem.setTotal(total);
        return cartItemRepository.save(cartItem);
    }
    
    private BigDecimal calculateTotal(BigDecimal price, BigDecimal quantity) {
        return price.multiply(quantity).setScale(2, HALF_UP);
    }
}
