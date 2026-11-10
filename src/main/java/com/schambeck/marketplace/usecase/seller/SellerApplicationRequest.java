package com.schambeck.marketplace.usecase.seller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SellerApplicationRequest {
    private String publicName;
    private String description;
    private String contactEmail;
    private String itemTypes;
    private String deliveryMethod;
    private String termsVersion;
    private Boolean termsAccepted;
}
