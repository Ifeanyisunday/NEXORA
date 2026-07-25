ALTER TABLE transfers
ADD COLUMN reference VARCHAR(100) NOT NULL;

ALTER TABLE transfers
ADD CONSTRAINT uk_transfer_reference
UNIQUE (reference);