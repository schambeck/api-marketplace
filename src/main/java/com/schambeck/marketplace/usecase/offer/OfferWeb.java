package com.schambeck.marketplace.usecase.offer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OfferWeb {
    private Integer id;
    private Integer productId;
    private String game;
    private String platform;
    private String region;
    private String variant;
    private BigDecimal price;
    private String deliveryMethod;
    private OfferAvailabilityStatus availabilityStatus;
    private OfferStatus status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
