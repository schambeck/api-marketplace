package com.schambeck.marketplace.usecase.mercadopago.payment;

import com.schambeck.marketplace.usecase.cart.CartWeb;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PaymentWeb {
	private Integer id;
	private CartWeb cart;
	private Long paymentId;
	private String status;
	private String externalReference;
	private Long merchantOrderId;
	private PaymentDetailWeb detail;
	private Integer version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
