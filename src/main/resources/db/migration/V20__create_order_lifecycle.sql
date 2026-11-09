CREATE TABLE "order"
(
    id SERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    buyer_id INTEGER NOT NULL REFERENCES "user" (id),
    cart_id INTEGER REFERENCES cart (id),
    checkout_key VARCHAR(255) NOT NULL,
    status VARCHAR(40) NOT NULL,
    subtotal_amount DECIMAL(12,2) NOT NULL CHECK (subtotal_amount >= 0),
    discount_total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (discount_total >= 0),
    tax_total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (tax_total >= 0),
    shipping_total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (shipping_total >= 0),
    fee_total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (fee_total >= 0),
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    buyer_snapshot JSONB NOT NULL,
    instructions JSONB,
    payment_reference VARCHAR(255), inventory_reference VARCHAR(255),
    fulfillment_reference VARCHAR(255), delivery_reference VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    UNIQUE (buyer_id, checkout_key)
);

CREATE TABLE order_item
(
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES "order" (id),
    product_id INTEGER NOT NULL REFERENCES product (id),
    offer_id INTEGER NOT NULL REFERENCES offer (id),
    seller_id INTEGER NOT NULL REFERENCES "user" (id),
    product_name VARCHAR(255) NOT NULL, product_description TEXT,
    context_game VARCHAR(255) NOT NULL, context_platform VARCHAR(255) NOT NULL,
    context_region VARCHAR(255), context_variant VARCHAR(255),
    delivery_method VARCHAR(255) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL CHECK (unit_price >= 0),
    quantity DECIMAL(12,2) NOT NULL CHECK (quantity > 0),
    subtotal_amount DECIMAL(12,2) NOT NULL CHECK (subtotal_amount >= 0),
    snapshot_version INTEGER NOT NULL DEFAULT 1,
    snapshot JSONB NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX idx_order_buyer_created ON "order" (buyer_id, created_at DESC);
CREATE INDEX idx_order_item_order ON order_item (order_id);

CREATE TABLE order_status_history
(
    id BIGSERIAL PRIMARY KEY, order_id INTEGER NOT NULL REFERENCES "order" (id),
    from_status VARCHAR(40), to_status VARCHAR(40) NOT NULL,
    cause VARCHAR(255) NOT NULL, source VARCHAR(255) NOT NULL, actor VARCHAR(255),
    idempotency_key VARCHAR(255), metadata JSONB, created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    UNIQUE (order_id, idempotency_key)
);
CREATE INDEX idx_order_history_order_created ON order_status_history (order_id, created_at);
