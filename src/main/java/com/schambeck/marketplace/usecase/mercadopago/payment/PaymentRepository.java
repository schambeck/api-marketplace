package com.schambeck.marketplace.usecase.mercadopago.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Page<Payment> findAllByOrderByIdAsc(Pageable pageable);

    Page<Payment> findAllByStatusContainingIgnoreCaseOrderByIdAsc(Pageable pageable, String description);
}
