package com.schambeck.marketplace.usecase.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository repository;

    @Transactional
    public Product create(Product entity) {
        return repository.save(entity);
    }

    @Transactional
    public Product update(int id, Product entity) {
        Product toUpdate = findById(id);
        ProductMapper.INSTANCE.copy(entity, toUpdate);
        return repository.save(toUpdate);
    }

    @Transactional
    public void remove(int id) {
        repository.deleteById(id);
    }

    public Product findById(int id) {
        return repository.findByIdOrThrow(id);
    }

    public Page<Product> search(Pageable pageable, String text) {
        if (text == null || text.isBlank()) {
            return repository.findAllByOrderByIdAsc(pageable);
        } else {
            return repository.findAllByNameContainingIgnoreCaseOrderByIdAsc(pageable, text);
        }
    }
}
