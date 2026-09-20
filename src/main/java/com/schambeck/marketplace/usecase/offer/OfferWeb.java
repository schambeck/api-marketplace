package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.usecase.gamecontext.GameContextReference;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OfferWeb {
    private Integer id;
    private Integer productId;
    private Integer gameContextId;
    private Integer contextVersion;
    private GameContextReference context;
    private BigDecimal price;
    private String deliveryMethod;
    private String availabilitySource;
    private OfferAvailabilityStatus availabilityStatus;
    private OfferStatus status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
