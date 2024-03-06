package com.schambeck.marketplace.usecase.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository repository;

    @Transactional
    public Cart create(Cart entity) {
        return repository.save(entity);
    }

    @Transactional
    public Cart update(int id, Cart entity) {
        Cart toUpdate = findById(id);
        CartMapper.INSTANCE.copy(entity, toUpdate);
        return repository.save(toUpdate);
    }

    @Transactional
    public void remove(int id) {
        repository.deleteById(id);
    }

    public Cart findById(int id) {
        return repository.findByIdOrThrow(id);
    }

    public Page<Cart> search(Pageable pageable, String text) {
        if (text == null || text.isBlank()) {
            return repository.findAllByOrderByIdAsc(pageable);
        } else {
            return repository.findAllByUser_FullNameContainingIgnoreCaseOrderByIdAsc(pageable, text);
        }
    }
}
