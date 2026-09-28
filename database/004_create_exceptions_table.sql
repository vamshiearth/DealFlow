CREATE TABLE exceptions (
    id BIGSERIAL PRIMARY KEY,
    deal_id BIGINT NOT NULL,
    exception_type VARCHAR(50) NOT NULL,
    requested_value NUMERIC(15, 2) NOT NULL,
    standard_value NUMERIC(15, 2) NOT NULL,
    justification VARCHAR(2000) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_by BIGINT NULL,
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_exception_deal FOREIGN KEY (deal_id)
        REFERENCES deals(id) ON DELETE CASCADE
);