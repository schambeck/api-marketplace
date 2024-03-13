package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public Product create(Product entity) {
        return repository.save(entity);
    }

    @Transactional
    public Product create(Product entity, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        entity.setUser(user);
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

    public Page<Product> searchByUser(Pageable pageable, String text, String username) {
        if (text == null || text.isBlank()) {
            return repository.findAllByUser_UsernameOrderByIdAsc(pageable, username);
        } else {
            return repository.findAllByUser_UsernameAndNameContainingIgnoreCaseOrderByIdAsc(pageable, text, username);
        }
    }
}
