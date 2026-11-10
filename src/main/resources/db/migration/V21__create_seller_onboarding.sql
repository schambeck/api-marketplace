CREATE TABLE seller_application
(
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES "user" (id),
    public_name VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    item_types VARCHAR(500) NOT NULL,
    delivery_method VARCHAR(255) NOT NULL,
    terms_version VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'WITHDRAWN')),
    decision_reason VARCHAR(1000),
    decided_by VARCHAR(255),
    submitted_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    version INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_seller_application_status_submitted ON seller_application (status, submitted_at);
CREATE INDEX idx_seller_application_user ON seller_application (user_id, submitted_at DESC);
CREATE UNIQUE INDEX uq_seller_application_pending_user ON seller_application (user_id) WHERE status = 'PENDING';

CREATE TABLE seller_application_history
(
    id BIGSERIAL PRIMARY KEY,
    application_id INTEGER NOT NULL REFERENCES seller_application (id),
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    reason VARCHAR(1000),
    actor VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);
