package com.schambeck.marketplace.usecase.mercadopago.payment;

import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.order.Order;
import com.schambeck.marketplace.usecase.order.OrderRepository;
import com.schambeck.marketplace.usecase.order.OrderStatus;
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
    private final OrderRepository orderRepository;

    @Transactional
    public Payment create(Payment entity) {
        try {
            Order order = orderRepository.findByPublicId(java.util.UUID.fromString(entity.getExternalReference()))
                    .orElseThrow();
            entity.setOrder(order);
            entity.setCart(cartRepository.findByIdOrThrow(order.getCartId()));
            if ("approved".equalsIgnoreCase(entity.getStatus())) {
                order.setPaymentReference(String.valueOf(entity.getPaymentId()));
                order.setStatus(OrderStatus.PAID);
            } else if ("rejected".equalsIgnoreCase(entity.getStatus())) {
                order.setPaymentReference(String.valueOf(entity.getPaymentId()));
                order.setStatus(OrderStatus.REJECTED);
            }
        } catch (IllegalArgumentException | java.util.NoSuchElementException legacyReference) {
            // Existing Mercado Pago records use Cart.id. Keep that data readable during migration.
            entity.setCart(cartRepository.findByIdOrThrow(Integer.valueOf(entity.getExternalReference())));
        }
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
