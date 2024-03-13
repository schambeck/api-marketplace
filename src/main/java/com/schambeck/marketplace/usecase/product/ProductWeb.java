package com.schambeck.marketplace.usecase.product;

import com.schambeck.marketplace.usecase.user.UserWeb;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ProductWeb {
	private int id;
	private String name;
	private String description;
	private BigDecimal price;
	private BigDecimal quantity;
	private UserWeb user;
	private String category;
	private String inventoryStatus;
	private String rating;
	private String image;
	private String objectId;
	private Integer version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
