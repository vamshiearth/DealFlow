ALTER TABLE approval_requests
ADD COLUMN approval_cycle INTEGER NOT NULL DEFAULT 1;

ALTER TABLE approval_requests
DROP CONSTRAINT uq_deal_approval_sequence;

ALTER TABLE approval_requests
ADD CONSTRAINT uq_deal_approval_cycle_sequence
UNIQUE (deal_id, approval_cycle, sequence);