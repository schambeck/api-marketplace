package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    java.util.Optional<Product> findByIdForUpdate(@Param("id") Integer id);
    Page<Product> findAllByOrderByIdAsc(Pageable pageable);

    Page<Product> findAllByNameContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
    
    Page<Product> findAllByUser_UsernameOrderByIdAsc(Pageable pageable, String username);

    Page<Product> findAllByUser_UsernameAndNameContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String name, String username);
    
    default Product findByIdOrThrow(Integer id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(Product.class, id));
    }
}
