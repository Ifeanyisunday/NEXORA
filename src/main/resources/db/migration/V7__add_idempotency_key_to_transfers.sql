ALTER TABLE transfers
ADD COLUMN idempotency_key VARCHAR(100) NOT NULL;

ALTER TABLE transfers
ADD CONSTRAINT uk_transfer_idempotency_key
UNIQUE (idempotency_key);