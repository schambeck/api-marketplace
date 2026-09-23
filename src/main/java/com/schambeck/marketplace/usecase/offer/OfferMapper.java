package com.schambeck.marketplace.usecase.offer;

public final class OfferMapper {
    private OfferMapper() {
    }

    public static OfferWeb toWeb(Offer offer) {
        OfferWeb web = new OfferWeb();
        web.setId(offer.getId());
        web.setProductId(offer.getProduct().getId());
        web.setGame(offer.getContextGame());
        web.setPlatform(offer.getContextPlatform());
        web.setRegion(offer.getContextRegion());
        web.setVariant(offer.getContextVariant());
        web.setPrice(offer.getPrice());
        web.setDeliveryMethod(offer.getDeliveryMethod());
        web.setAvailabilityStatus(offer.getAvailabilityStatus());
        web.setStatus(offer.getStatus());
        web.setVersion(offer.getVersion());
        web.setCreatedAt(offer.getCreatedAt());
        web.setModifiedAt(offer.getModifiedAt());
        return web;
    }

    public static SellerOfferWeb toSellerWeb(Offer offer) {
        SellerOfferWeb web = new SellerOfferWeb();
        web.setId(offer.getId());
        web.setProductId(offer.getProduct().getId());
        web.setGame(offer.getContextGame());
        web.setPlatform(offer.getContextPlatform());
        web.setRegion(offer.getContextRegion());
        web.setVariant(offer.getContextVariant());
        web.setPrice(offer.getPrice());
        web.setDeliveryMethod(offer.getDeliveryMethod());
        web.setAvailabilityStatus(offer.getAvailabilityStatus());
        web.setStatus(offer.getStatus());
        web.setVersion(offer.getVersion());
        web.setCreatedAt(offer.getCreatedAt());
        web.setModifiedAt(offer.getModifiedAt());
        return web;
    }
}
