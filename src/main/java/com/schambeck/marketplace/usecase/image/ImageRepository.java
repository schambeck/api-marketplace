package com.schambeck.marketplace.usecase.image;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    Page<Image> findAllByOrderByIdAsc(Pageable pageable);

    Page<Image> findAllByProduct_DescriptionContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
    
    List<Image> findAllByProduct_IdOrderByIdAsc(Integer productId);
}
