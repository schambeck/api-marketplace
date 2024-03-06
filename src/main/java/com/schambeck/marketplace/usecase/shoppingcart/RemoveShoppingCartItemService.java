package com.schambeck.marketplace.usecase.shoppingcart;

import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RemoveShoppingCartItemService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public void remove(Integer cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdOrThrow(cartItemId);
        cartItemRepository.delete(cartItem);
        Cart cart = cartItem.getCart();
        boolean exists = cartItemRepository.existsByCart_Id(cart.getId());
        if (exists) {
            cart.setTotal(cart.getTotal().subtract(cartItem.getTotal()));
        } else {
            cartRepository.deleteById(cart.getId());
        }
    }
}
