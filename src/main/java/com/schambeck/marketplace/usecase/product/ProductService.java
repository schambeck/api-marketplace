package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserRepository;
import com.schambeck.marketplace.exception.InventoryValidationException;
import com.schambeck.marketplace.usecase.inventory.InventoryBalance;
import com.schambeck.marketplace.usecase.inventory.InventoryBalanceRepository;
import java.math.BigDecimal;
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
    private final InventoryBalanceRepository inventoryBalanceRepository;

    @Transactional
    public Product create(Product entity) {
        Product product = repository.save(entity);
        initializeInventory(product);
        return product;
    }

    @Transactional
    public Product create(Product entity, String username) {
        User user = userRepository.findByUsernameOrThrow(username);
        entity.setUser(user);
        Product product = repository.save(entity);
        initializeInventory(product);
        return product;
    }

    @Transactional
    public Product update(int id, Product entity) {
        Product toUpdate = findById(id);
        if (entity.getQuantity() != null && (toUpdate.getQuantity() == null
                || entity.getQuantity().compareTo(toUpdate.getQuantity()) != 0)) {
            throw new InventoryValidationException("Product quantity must be changed through inventory operations.");
        }
        ProductMapper.INSTANCE.copy(entity, toUpdate);
        return repository.save(toUpdate);
    }

    private void initializeInventory(Product product) {
        if (!inventoryBalanceRepository.existsByProductId(product.getId())) {
            inventoryBalanceRepository.save(InventoryBalance.builder()
                    .product(product)
                    .availableQuantity(product.getQuantity() == null ? BigDecimal.ZERO : product.getQuantity())
                    .reservedQuantity(BigDecimal.ZERO)
                    .allocatedQuantity(BigDecimal.ZERO)
                    .consumedQuantity(BigDecimal.ZERO)
                    .version(0)
                    .build());
        }
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
