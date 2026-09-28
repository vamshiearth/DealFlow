CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,

    deal_id BIGINT NOT NULL,

    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NULL,

    event_type VARCHAR(100) NOT NULL,

    actor_type VARCHAR(20) NOT NULL,
    actor_user_id BIGINT NULL,

    old_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NULL,

    description VARCHAR(2000) NULL,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_audit_deal
        FOREIGN KEY (deal_id)
        REFERENCES deals(id),

    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_user_id)
        REFERENCES users(id)
);

GRANT SELECT, INSERT
ON TABLE audit_events
TO "user";

GRANT USAGE, SELECT
ON SEQUENCE audit_events_id_seq
TO "user";
