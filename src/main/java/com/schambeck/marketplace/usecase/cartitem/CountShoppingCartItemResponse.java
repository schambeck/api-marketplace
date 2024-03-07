package com.schambeck.marketplace.usecase.cartitem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CountShoppingCartItemResponse {
	private final Integer quantity;
}
