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

    public Page<CartItem> search(Pageable pageable, String text) {
        if (text == null || text.isBlank()) {
            return repository.findAllByOrderByIdAsc(pageable);
        } else {
            return repository.findAllByProduct_DescriptionContainingIgnoreCaseOrderByIdAsc(pageable, text);
        }
    }

    public Page<CartItem> searchByUsername(Pageable pageable, String text, String username) {
        if (text == null || text.isBlank()) {
            return repository.findAllByCart_User_UsernameOrderByIdDesc(pageable, username);
        } else {
            return repository.findAllByProduct_DescriptionContainingIgnoreCaseAndCart_User_UsernameOrderByIdDesc(pageable, text, username);
        }
    }
    
    public int countByUsername(String username) {
        return repository.countByCart_User_Username(username);
    }
}
