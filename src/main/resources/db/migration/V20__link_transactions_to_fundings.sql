-- Link each wallet-funding ledger entry to its originating funding record.
-- Nullable so transfers, withdrawals, and other non-funding transactions remain valid.
ALTER TABLE transactions
    ADD COLUMN funding_id UUID;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transaction_funding
        FOREIGN KEY (funding_id)
        REFERENCES fundings (id);

-- PostgreSQL permits multiple NULL values in a UNIQUE constraint, while
-- preventing two non-NULL rows from referencing the same funding.
ALTER TABLE transactions
    ADD CONSTRAINT uk_transaction_funding_id
        UNIQUE (funding_id);