package com.schambeck.marketplace.usecase.cartitem;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class UpdateShoppingCartItemRequest {
	private BigDecimal quantity;
	
	public UpdateShoppingCartItemRequest() {
	}
	
	public UpdateShoppingCartItemRequest(BigDecimal quantity) {
		this.quantity = quantity;
	}
}
