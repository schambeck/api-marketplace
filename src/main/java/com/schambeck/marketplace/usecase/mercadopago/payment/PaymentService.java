package com.schambeck.marketplace.usecase.mercadopago.payment;

import com.schambeck.marketplace.usecase.cart.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository repository;
    private final CartRepository cartRepository;

    @Transactional
    public Payment create(Payment entity) {
        Integer cartId = Integer.valueOf(entity.getExternalReference());
        entity.setCart(cartRepository.findByIdOrThrow(cartId));
        return repository.save(entity);
    }

    @Transactional
    public Payment update(int id, Payment entity) {
        Payment toUpdate = findById(id);
        PaymentMapper.INSTANCE.copy(entity, toUpdate);
        return repository.save(toUpdate);
    }

    @Transactional
    public void remove(int id) {
        repository.deleteById(id);
    }

    public Payment findById(int id) {
        return repository.findById(id).orElseThrow();
    }

    public Page<Payment> search(Pageable pageable, String text) {
        if (text == null || text.isBlank()) {
            return repository.findAllByOrderByIdAsc(pageable);
        } else {
            return repository.findAllByStatusContainingIgnoreCaseOrderByIdAsc(pageable, text);
        }
    }
}
