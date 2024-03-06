package com.schambeck.marketplace.usecase.cartitem;

import com.schambeck.marketplace.usecase.cart.CartWeb;

import com.schambeck.marketplace.usecase.product.ProductWeb;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CartItemWeb {
	private int id;
	private CartWeb cart;
	private ProductWeb product;
	private BigDecimal price;
	private BigDecimal quantity;
	private BigDecimal total;
	private Integer version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
