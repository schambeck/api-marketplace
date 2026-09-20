package com.schambeck.marketplace.usecase.offer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferActionRequest {
    private String reason;
    private String source;
}
