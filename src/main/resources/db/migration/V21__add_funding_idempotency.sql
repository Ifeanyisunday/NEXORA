-- Add an idempotency key to funding requests.
ALTER TABLE fundings
ADD COLUMN idempotency_key VARCHAR(100);

-- Give existing funding records unique legacy keys.
-- This allows us to make the new column NOT NULL safely.
UPDATE fundings
SET idempotency_key = 'LEGACY-' || id::TEXT
WHERE idempotency_key IS NULL;

-- Every funding record must have an idempotency key.
ALTER TABLE fundings
ALTER COLUMN idempotency_key SET NOT NULL;

-- A user cannot reuse an idempotency key for another funding request.
-- Different users may use the same key.
ALTER TABLE fundings
ADD CONSTRAINT uk_funding_user_idempotency
UNIQUE (user_id, idempotency_key);