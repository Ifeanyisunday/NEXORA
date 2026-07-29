ALTER TABLE transactions
ADD COLUMN transfer_id UUID;

ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_transfer
FOREIGN KEY (transfer_id)
REFERENCES transfers(id);

CREATE INDEX idx_transaction_transfer
ON transactions(transfer_id);