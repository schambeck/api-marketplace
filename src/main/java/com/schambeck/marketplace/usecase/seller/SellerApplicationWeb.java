package com.schambeck.marketplace.usecase.seller;

import java.time.LocalDateTime;

public record SellerApplicationWeb(Integer id, String publicName, String description, String contactEmail,
                                   String itemTypes, String deliveryMethod, String termsVersion,
                                   SellerApplicationStatus status, String decisionReason,
                                   String decidedBy, LocalDateTime submittedAt, LocalDateTime modifiedAt) {
    static SellerApplicationWeb from(SellerApplication a) {
        return new SellerApplicationWeb(a.getId(), a.getPublicName(), a.getDescription(), a.getContactEmail(),
                a.getItemTypes(), a.getDeliveryMethod(), a.getTermsVersion(), a.getStatus(),
                a.getDecisionReason(), a.getDecidedBy(), a.getSubmittedAt(), a.getModifiedAt());
    }
}
