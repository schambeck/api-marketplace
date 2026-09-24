CREATE TABLE inventory_balance
(
    id                 SERIAL PRIMARY KEY,
    product_id         INTEGER NOT NULL UNIQUE REFERENCES product (id),
    available_quantity DECIMAL(12,2) NOT NULL CHECK (available_quantity >= 0),
    reserved_quantity  DECIMAL(12,2) NOT NULL CHECK (reserved_quantity >= 0),
    allocated_quantity DECIMAL(12,2) NOT NULL CHECK (allocated_quantity >= 0),
    consumed_quantity  DECIMAL(12,2) NOT NULL CHECK (consumed_quantity >= 0),
    version            INTEGER NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified_at        TIMESTAMP NOT NULL DEFAULT current_timestamp
);

INSERT INTO inventory_balance
    (product_id, available_quantity, reserved_quantity, allocated_quantity, consumed_quantity)
SELECT id, quantity, 0, 0, 0
FROM product;

CREATE TABLE inventory_reservation
(
    id             SERIAL PRIMARY KEY,
    order_item_id  INTEGER NOT NULL,
    product_id     INTEGER NOT NULL REFERENCES product (id),
    seller_id      INTEGER NOT NULL REFERENCES "user" (id),
    quantity       DECIMAL(12,2) NOT NULL CHECK (quantity > 0),
    status         VARCHAR(32) NOT NULL,
    operation_key  VARCHAR(255) NOT NULL UNIQUE,
    expires_at     TIMESTAMP NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified_at    TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX idx_inventory_reservation_expiration
    ON inventory_reservation (status, expires_at);

CREATE TABLE inventory_allocation
(
    id               SERIAL PRIMARY KEY,
    reservation_id   INTEGER NOT NULL REFERENCES inventory_reservation (id),
    quantity         DECIMAL(12,2) NOT NULL CHECK (quantity > 0),
    consumed_quantity DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (consumed_quantity >= 0),
    status           VARCHAR(32) NOT NULL,
    operation_key    VARCHAR(255) NOT NULL UNIQUE,
    created_at       TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified_at      TIMESTAMP NOT NULL DEFAULT current_timestamp,
    CHECK (consumed_quantity <= quantity)
);

CREATE TABLE inventory_movement
(
    id              BIGSERIAL PRIMARY KEY,
    product_id      INTEGER NOT NULL REFERENCES product (id),
    reservation_id  INTEGER REFERENCES inventory_reservation (id),
    allocation_id   INTEGER REFERENCES inventory_allocation (id),
    operation       VARCHAR(64) NOT NULL,
    from_status     VARCHAR(32),
    to_status       VARCHAR(32) NOT NULL,
    quantity        DECIMAL(12,2) NOT NULL CHECK (quantity > 0),
    operation_key   VARCHAR(255) NOT NULL,
    actor           VARCHAR(255),
    source          VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX idx_inventory_movement_product ON inventory_movement (product_id, id);
CREATE UNIQUE INDEX uq_inventory_movement_operation ON inventory_movement (operation_key, operation);
