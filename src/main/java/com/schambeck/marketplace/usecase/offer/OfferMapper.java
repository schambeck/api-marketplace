package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.usecase.gamecontext.GameContextReference;

public final class OfferMapper {
    private OfferMapper() {
    }

    public static OfferWeb toWeb(Offer offer) {
        OfferWeb web = new OfferWeb();
        web.setId(offer.getId());
        web.setProductId(offer.getProduct().getId());
        web.setGameContextId(offer.getGameContext().getId());
        web.setContextVersion(offer.getContextVersion());
        web.setContext(new GameContextReference(offer.getGameContext().getId(), offer.getContextVersion(),
                offer.getContextGame(), offer.getContextPublisher(), offer.getContextPlatform(),
                offer.getContextRegion(), offer.getContextServer(), offer.getContextVariant()));
        web.setPrice(offer.getPrice());
        web.setDeliveryMethod(offer.getDeliveryMethod());
        web.setAvailabilitySource(offer.getAvailabilitySource());
        web.setAvailabilityStatus(offer.getAvailabilityStatus());
        web.setStatus(offer.getStatus());
        web.setVersion(offer.getVersion());
        web.setCreatedAt(offer.getCreatedAt());
        web.setModifiedAt(offer.getModifiedAt());
        return web;
    }
}
