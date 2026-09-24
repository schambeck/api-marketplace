CREATE TABLE inventory_operation
(
    id             BIGSERIAL PRIMARY KEY,
    reservation_id INTEGER NOT NULL REFERENCES inventory_reservation (id),
    allocation_id  INTEGER REFERENCES inventory_allocation (id),
    operation_type VARCHAR(32) NOT NULL,
    operation_key  VARCHAR(255) NOT NULL,
    quantity       DECIMAL(12,2),
    created_at     TIMESTAMP NOT NULL DEFAULT current_timestamp,
    CONSTRAINT uq_inventory_operation_key UNIQUE (operation_type, operation_key)
);

CREATE INDEX idx_inventory_operation_reservation
    ON inventory_operation (reservation_id, operation_type);
