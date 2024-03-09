package com.schambeck.marketplace.usecase.cartitem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateIfAbsentShoppingCartItemResponse {
	private final Boolean created;
	private final CartItemWeb cartItem;
}
