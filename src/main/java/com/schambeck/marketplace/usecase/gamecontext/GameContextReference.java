package com.schambeck.marketplace.usecase.gamecontext;

/**
 * Immutable contract for consumers that must preserve the context used by an offer or order.
 */
public record GameContextReference(
        Integer id,
        Integer version,
        String game,
        String publisher,
        String platform,
        String region,
        String server,
        String variant
) {
}
