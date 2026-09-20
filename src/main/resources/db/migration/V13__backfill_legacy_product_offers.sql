-- Operational backfill for products created before Offer existed.
-- The LEGACY context is deliberately explicit and active so migrated products
-- remain addressable while consumers move to the offer contract. Operators can
-- replace this context and revise the offers with the normal admin API.
INSERT INTO game_context
    (game, publisher, platform, region, server, variant, status, version, created_at, modified_at)
SELECT 'LEGACY', 'LEGACY_PRODUCT_MIGRATION', 'LEGACY', NULL, NULL, NULL,
       'ACTIVE', 0, current_timestamp, current_timestamp
WHERE NOT EXISTS (
    SELECT 1 FROM game_context
    WHERE game = 'LEGACY' AND publisher = 'LEGACY_PRODUCT_MIGRATION' AND platform = 'LEGACY'
);

INSERT INTO offer
    (product_id, game_context_id, context_version, context_game, context_publisher,
     context_platform, context_region, context_server, context_variant, price,
     delivery_method, availability_source, availability_status, status, version,
     created_at, modified_at)
SELECT p.id,
       gc.id,
       gc.version,
       gc.game,
       gc.publisher,
       gc.platform,
       gc.region,
       gc.server,
       gc.variant,
       p.price,
       'LEGACY',
       'LEGACY_PRODUCT_MIGRATION',
       CASE
           WHEN p.quantity > 0
                AND upper(coalesce(p.inventory_status, '')) NOT IN ('OUTOFSTOCK', 'OUT_OF_STOCK')
           THEN 'AVAILABLE'
           ELSE 'UNAVAILABLE'
       END,
       'PUBLISHED',
       0,
       current_timestamp,
       current_timestamp
FROM product p
JOIN game_context gc
  ON gc.game = 'LEGACY'
 AND gc.publisher = 'LEGACY_PRODUCT_MIGRATION'
 AND gc.platform = 'LEGACY'
WHERE p.price > 0
  AND NOT EXISTS (SELECT 1 FROM offer existing WHERE existing.product_id = p.id);

INSERT INTO offer_status_history
    (offer_id, from_status, to_status, reason, source, changed_by, recorded_at)
SELECT o.id,
       NULL,
       o.status,
       'backfill legacy product offer',
       'LEGACY_PRODUCT_MIGRATION',
       'flyway',
       current_timestamp
FROM offer o
WHERE o.availability_source = 'LEGACY_PRODUCT_MIGRATION'
  AND NOT EXISTS (
      SELECT 1 FROM offer_status_history history
      WHERE history.offer_id = o.id
        AND history.source = 'LEGACY_PRODUCT_MIGRATION'
  );
