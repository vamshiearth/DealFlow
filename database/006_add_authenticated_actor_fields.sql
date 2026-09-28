ALTER TABLE approval_requests
ADD COLUMN resolved_by BIGINT NULL;

ALTER TABLE approval_requests
ADD CONSTRAINT fk_approval_resolved_by
FOREIGN KEY (resolved_by)
REFERENCES users(id);

ALTER TABLE exceptions
ADD CONSTRAINT fk_exception_created_by
FOREIGN KEY (created_by)
REFERENCES users(id);

ALTER TABLE exceptions
ADD COLUMN resolved_by BIGINT NULL;

ALTER TABLE exceptions
ADD CONSTRAINT fk_exception_resolved_by
FOREIGN KEY (resolved_by)
REFERENCES users(id);