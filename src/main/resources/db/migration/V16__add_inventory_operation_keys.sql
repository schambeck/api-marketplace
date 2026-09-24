ALTER TABLE inventory_reservation
    ADD COLUMN release_operation_key VARCHAR(255),
    ADD COLUMN expiration_operation_key VARCHAR(255);

CREATE UNIQUE INDEX uq_inventory_reservation_release_operation
    ON inventory_reservation (release_operation_key)
    WHERE release_operation_key IS NOT NULL;

CREATE UNIQUE INDEX uq_inventory_reservation_expiration_operation
    ON inventory_reservation (expiration_operation_key)
    WHERE expiration_operation_key IS NOT NULL;

CREATE OR REPLACE FUNCTION reject_inventory_movement_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'inventory_movement is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER inventory_movement_append_only
    BEFORE UPDATE OR DELETE ON inventory_movement
    FOR EACH ROW EXECUTE FUNCTION reject_inventory_movement_mutation();
