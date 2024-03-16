package com.schambeck.marketplace.usecase.mercadopago.webhook;

import com.schambeck.marketplace.usecase.mercadopago.payment.Payment;
import com.schambeck.marketplace.usecase.mercadopago.payment.PaymentMapper;
import com.schambeck.marketplace.usecase.mercadopago.payment.PaymentService;
import com.schambeck.marketplace.usecase.mercadopago.payment.PaymentWeb;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping("webhook/payment")
//@RequiredArgsConstructor
public class PaymentWebhook {
//    private static final PaymentMapper mapper = PaymentMapper.INSTANCE;
//    private final PaymentService service;
//
//    @PostMapping
//    public ResponseEntity<PaymentWeb> create(@RequestBody PaymentWeb web) {
//        Payment created = service.create(mapper.toEntity(web));
//        return ResponseEntity.ok(mapper.toWeb(created));
//    }
}
