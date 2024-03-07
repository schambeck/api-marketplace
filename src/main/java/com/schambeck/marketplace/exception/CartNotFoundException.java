package com.schambeck.marketplace.exception;

import com.schambeck.marketplace.usecase.cart.Cart;

public class CartNotFoundException extends EntityNotFoundException {
    public CartNotFoundException(Integer id) {
        super(Cart.class, id);
    }
    
    public CartNotFoundException(String username) {
        super(Cart.class + " username " + username + " not found.");
    }
}
