package com.schambeck.marketplace.usecase.mercadopago.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetail implements Serializable {
	private Long collectionId;
	private String collectionStatus;
	private String paymentType;
	private String preferenceId;
	private String siteId;
	private String processingMode;
	private Long merchantAccountId;
}
