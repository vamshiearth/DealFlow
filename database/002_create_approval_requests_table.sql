CREATE TABLE approval_requests (
    id BIGSERIAL PRIMARY KEY,

    deal_id BIGINT NOT NULL,

    approver_role VARCHAR(50) NOT NULL,

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    sequence INTEGER NOT NULL,

    requested_at TIMESTAMP NOT NULL,

    resolved_at TIMESTAMP NULL,

    comments VARCHAR(2000),

    CONSTRAINT fk_approval_request_deal
        FOREIGN KEY (deal_id)
        REFERENCES deals(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_deal_approval_sequence
        UNIQUE (deal_id, sequence)
);