package com.schambeck.marketplace.usecase.cart;

import com.schambeck.marketplace.exception.CartNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Page<Cart> findAllByOrderByIdAsc(Pageable pageable);

    Page<Cart> findAllByUser_FullNameContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
    
    default Cart findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new CartNotFoundException(id));
    }
    
    Optional<Cart> findByUser_Username(String username);
    
    default Cart findByUser_UsernameOrThrow(String username) {
        return findByUser_Username(username).orElseThrow(() -> new CartNotFoundException(username));
    }
}
