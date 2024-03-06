package com.schambeck.marketplace.usecase.cartitem;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class CreateShoppingCartItemRequest {
	private Integer productId;
	private BigDecimal quantity;
	private String username;
	
	public CreateShoppingCartItemRequest(Integer productId, BigDecimal quantity, String username) {
		this.productId = productId;
		this.quantity = quantity;
		this.username = username;
	}
}
