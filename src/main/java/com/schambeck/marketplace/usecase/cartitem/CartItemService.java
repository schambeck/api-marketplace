package com.schambeck.marketplace.usecase.cartitem;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {
    private final CartItemRepository repository;

    @Transactional
    public CartItem create(CartItem entity) {
        return repository.save(entity);
    }

    @Transactional
    public CartItem update(int id, CartItem entity) {
        CartItem toUpdate = findById(id);
        CartItemMapper.INSTANCE.copy(entity, toUpdate);
        return repository.save(toUpdate);
    }

    @Transactional
    public void remove(int id) {
        repository.deleteById(id);
    }

    public CartItem findById(int id) {
        return repository.findByIdOrThrow(id);
    }

    public Page<CartItem> searchByCart(Pageable pageable, String text) {
        if (text == null || text.isBlank()) {
            return repository.findAllByOrderByIdAsc(pageable);
        } else {
            return repository.findAllByProduct_DescriptionContainingIgnoreCaseOrderByIdAsc(pageable, text);
        }
    }

    public Page<CartItem> searchByCart(Pageable pageable, String text, Integer cartId) {
        if (text == null || text.isBlank()) {
            return repository.findAllByCart_IdOrderByIdAsc(pageable, cartId);
        } else {
            return repository.findAllByProduct_DescriptionContainingIgnoreCaseAndCart_IdOrderByIdAsc(pageable, text, cartId);
        }
    }
}
