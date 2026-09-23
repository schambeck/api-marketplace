package com.schambeck.marketplace.usecase.offer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OfferUpdateRequest {
    private Integer productId;
    private String game;
    private String platform;
    private String region;
    private String variant;
    private BigDecimal price;
    private String deliveryMethod;
    private String reason;
    private String source;
}
