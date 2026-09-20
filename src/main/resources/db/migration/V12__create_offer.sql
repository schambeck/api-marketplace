CREATE TABLE offer
(
    id                  SERIAL PRIMARY KEY,
    product_id          INTEGER      NOT NULL REFERENCES product (id),
    game_context_id     INTEGER      NOT NULL REFERENCES game_context (id),
    context_version     INTEGER      NOT NULL,
    context_game        VARCHAR(255) NOT NULL,
    context_publisher   VARCHAR(255),
    context_platform    VARCHAR(255) NOT NULL,
    context_region      VARCHAR(255),
    context_server      VARCHAR(255),
    context_variant     VARCHAR(255),
    price               DECIMAL(12,2) NOT NULL CHECK (price > 0),
    delivery_method     VARCHAR(255) NOT NULL,
    availability_source VARCHAR(255) NOT NULL,
    availability_status VARCHAR(32)  NOT NULL,
    status              VARCHAR(32)  NOT NULL,
    version             INTEGER      NOT NULL,
    created_at          TIMESTAMP,
    modified_at         TIMESTAMP
);

CREATE INDEX idx_offer_product ON offer (product_id);
CREATE INDEX idx_offer_context ON offer (game_context_id, context_version);
CREATE INDEX idx_offer_status_availability ON offer (status, availability_status);

CREATE TABLE offer_status_history
(
    id          SERIAL PRIMARY KEY,
    offer_id    INTEGER      NOT NULL REFERENCES offer (id),
    from_status VARCHAR(32),
    to_status   VARCHAR(32)  NOT NULL,
    reason      VARCHAR(255) NOT NULL,
    source      VARCHAR(255) NOT NULL,
    changed_by  VARCHAR(255),
    recorded_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_offer_status_history_offer ON offer_status_history (offer_id, id);

ALTER TABLE cart_item ADD COLUMN offer_id INTEGER REFERENCES offer (id);
CREATE INDEX idx_cart_item_offer ON cart_item (offer_id);
