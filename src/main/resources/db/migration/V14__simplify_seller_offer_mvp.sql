-- PLAN-021: keep legacy columns for rollback, but move the active offer model
-- to the product owner and the product's own quantity.
ALTER TABLE offer ALTER COLUMN game_context_id DROP NOT NULL;
ALTER TABLE offer ALTER COLUMN context_version DROP NOT NULL;
ALTER TABLE offer ALTER COLUMN availability_source DROP NOT NULL;

UPDATE offer offer_row
SET context_game = COALESCE(NULLIF(offer_row.context_game, ''), context.game),
    context_platform = COALESCE(NULLIF(offer_row.context_platform, ''), context.platform),
    context_region = COALESCE(offer_row.context_region, context.region),
    context_variant = COALESCE(offer_row.context_variant, context.variant)
FROM game_context context
WHERE offer_row.game_context_id = context.id;

UPDATE offer offer_row
SET availability_status = CASE
    WHEN product.quantity > 0 THEN 'AVAILABLE'
    ELSE 'UNAVAILABLE'
END
FROM product
WHERE offer_row.product_id = product.id;

WITH paused AS (
    UPDATE offer offer_row
    SET status = 'PAUSED'
    FROM product
    WHERE offer_row.product_id = product.id
      AND offer_row.status = 'PUBLISHED'
      AND (product.quantity IS NULL OR product.quantity <= 0
           OR offer_row.price IS NULL OR offer_row.price <= 0
           OR NULLIF(trim(offer_row.context_game), '') IS NULL
           OR NULLIF(trim(offer_row.context_platform), '') IS NULL
           OR offer_row.availability_source = 'LEGACY_PRODUCT_MIGRATION'
           OR upper(offer_row.context_game) = 'LEGACY')
    RETURNING offer_row.id
)
INSERT INTO offer_status_history
    (offer_id, from_status, to_status, reason, source, changed_by, recorded_at)
SELECT id, 'PUBLISHED', 'PAUSED',
       'pause invalid legacy offer during seller MVP migration',
       'PLAN-021_MIGRATION', 'flyway', current_timestamp
FROM paused;

CREATE INDEX IF NOT EXISTS idx_offer_public_stock
    ON offer (status, product_id);
