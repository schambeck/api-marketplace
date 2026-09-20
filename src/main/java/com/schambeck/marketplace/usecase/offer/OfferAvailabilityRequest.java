package com.schambeck.marketplace.usecase.offer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferAvailabilityRequest extends OfferActionRequest {
    private OfferAvailabilityStatus availabilityStatus;
}
