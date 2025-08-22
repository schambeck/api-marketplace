package com.schambeck.marketplace.usecase.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserWeb {
	private Integer id;
	private String username;
	private String email;
	private String fullName;
    private String publicKey;
    private LocalDateTime publicKeyModifiedAt;
	private Integer version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
