package com.schambeck.marketplace.usecase.cartitem;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Page<CartItem> findAllByOrderByIdAsc(Pageable pageable);
    
    Page<CartItem> findAllByCart_IdOrderByIdAsc(Pageable pageable, Integer cartId);

    Page<CartItem> findAllByProduct_DescriptionContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
    
    Page<CartItem> findAllByProduct_DescriptionContainingIgnoreCaseAndCart_IdOrderByIdAsc(Pageable pageable, String description, Integer cartId);
    
    default CartItem findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(CartItem.class, id));
    }
    
    boolean existsByCart_Id(Integer cartId);
}
