package com.schambeck.marketplace.usecase.offer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OfferUpdateRequest {
    private Integer productId;
    private Integer gameContextId;
    private BigDecimal price;
    private String deliveryMethod;
    private String availabilitySource;
    private OfferAvailabilityStatus availabilityStatus;
    private String reason;
    private String source;
}
