package com.schambeck.marketplace.usecase.image;

import com.schambeck.marketplace.usecase.product.ProductWeb;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ImageWeb {
	private Integer id;
	private ProductWeb product;
	private String objectId;
	private String fileName;
	private String contentType;
	private Long size;
	private Integer version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
