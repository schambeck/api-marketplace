package com.schambeck.marketplace.usecase.cartitem;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class CreateShoppingCartItemRequest {
	private Integer offerId;
	private Integer productId;
	private BigDecimal quantity;
	
	public CreateShoppingCartItemRequest(Integer productId, BigDecimal quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public CreateShoppingCartItemRequest() {
	}
}
