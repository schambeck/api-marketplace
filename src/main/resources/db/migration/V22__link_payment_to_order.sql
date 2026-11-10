ALTER TABLE payment ADD COLUMN order_id INTEGER REFERENCES "order" (id);
CREATE INDEX idx_payment_order ON payment (order_id);
