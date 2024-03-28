package com.schambeck.marketplace.usecase.image;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    private final ImageRepository repository;
    private final ImageMapper mapper;

    @Transactional
    public Image create(Image entity) {
        return repository.save(entity);
    }

    @Transactional
    public Image update(int id, Image entity) {
        Image toUpdate = findById(id);
        mapper.copy(entity, toUpdate);
        return repository.save(toUpdate);
    }

    @Transactional
    public void remove(int id) {
        repository.deleteById(id);
    }

    public Image findById(int id) {
        return repository.findById(id).orElseThrow();
    }

    public Page<Image> search(Pageable pageable, String text) {
        if (text == null || text.isBlank()) {
            return repository.findAllByOrderByIdAsc(pageable);
        } else {
            return repository.findAllByProduct_DescriptionContainingIgnoreCaseOrderByIdAsc(pageable, text);
        }
    }
    
    public List<Image> searchByProduct(int productId) {
        return repository.findAllByProduct_IdOrderByIdAsc(productId);
    }
}
