ALTER TABLE inventory_reservation
    ADD CONSTRAINT ck_inventory_reservation_quantity_scale
        CHECK (quantity = trunc(quantity, 2));

ALTER TABLE inventory_allocation
    ADD CONSTRAINT ck_inventory_allocation_quantity_scale
        CHECK (quantity = trunc(quantity, 2));

ALTER TABLE inventory_allocation
    ADD CONSTRAINT ck_inventory_allocation_consumed_scale
        CHECK (consumed_quantity = trunc(consumed_quantity, 2));

ALTER TABLE inventory_balance
    ADD CONSTRAINT ck_inventory_balance_quantity_scale
        CHECK (available_quantity = trunc(available_quantity, 2)
           AND reserved_quantity = trunc(reserved_quantity, 2)
           AND allocated_quantity = trunc(allocated_quantity, 2)
           AND consumed_quantity = trunc(consumed_quantity, 2));
