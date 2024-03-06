package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findAllByOrderByIdAsc(Pageable pageable);

    Page<Product> findAllByNameContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
    
    default Product findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(Product.class, id));
    }
}
