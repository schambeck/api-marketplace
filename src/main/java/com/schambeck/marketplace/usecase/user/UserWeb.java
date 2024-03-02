package com.schambeck.marketplace.usecase.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserWeb {
	private int id;
	private String username;
	private String email;
	private String fullName;
	private Integer version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
