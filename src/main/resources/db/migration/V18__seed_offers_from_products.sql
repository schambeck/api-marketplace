-- Seed offers for products created by V2__create_product.sql.
-- Existing offers from V13 are preserved and are not duplicated.
INSERT INTO offer
    (product_id, game_context_id, context_version, context_game,
     context_publisher, context_platform, context_region, context_server,
     context_variant, price, delivery_method, availability_source,
     availability_status, status, version, created_at, modified_at)
SELECT p.id,
       NULL,
       NULL,
       p.name,
       NULL,
       'PC',
       NULL,
       NULL,
       NULL,
       p.price,
       'digital',
       'V2_PRODUCT_SEED',
       CASE
           WHEN p.quantity > 0 THEN 'AVAILABLE'
           ELSE 'UNAVAILABLE'
       END,
       CASE
           WHEN p.quantity > 0 THEN 'PUBLISHED'
           ELSE 'PAUSED'
       END,
       0,
       current_timestamp,
       current_timestamp
FROM product p
WHERE p.price > 0
  AND NOT EXISTS (
      SELECT 1
      FROM offer existing
      WHERE existing.product_id = p.id
  );

INSERT INTO offer_status_history
    (offer_id, from_status, to_status, reason, source, changed_by, recorded_at)
SELECT o.id,
       NULL,
       o.status,
       'seed offer from V2 product',
       'V2_PRODUCT_SEED',
       'flyway',
       current_timestamp
FROM offer o
WHERE o.availability_source = 'V2_PRODUCT_SEED'
  AND NOT EXISTS (
      SELECT 1
      FROM offer_status_history history
      WHERE history.offer_id = o.id
        AND history.source = 'V2_PRODUCT_SEED'
  );
