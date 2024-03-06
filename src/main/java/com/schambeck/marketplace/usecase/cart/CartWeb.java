package com.schambeck.marketplace.usecase.cart;

import com.schambeck.marketplace.usecase.user.UserWeb;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CartWeb {
	private int id;
	private UserWeb user;
	private BigDecimal total;
	private Integer version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
