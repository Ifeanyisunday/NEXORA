-- ==========================================================
-- Migration : V10
-- Description: Create transactions table
-- Project: Nexora Banking API
-- ==========================================================


CREATE TABLE transactions (

    id UUID NOT NULL,

    wallet_id UUID NOT NULL,

    type VARCHAR(20) NOT NULL,

    category VARCHAR(50) NOT NULL,

    amount NUMERIC(19,2) NOT NULL,

    balance_before NUMERIC(19,2) NOT NULL,

    balance_after NUMERIC(19,2) NOT NULL,

    reference VARCHAR(100) NOT NULL,

    description VARCHAR(255),

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_transactions
        PRIMARY KEY (id),

    CONSTRAINT fk_transactions_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id),

    CONSTRAINT uk_transaction_reference
        UNIQUE (reference),

    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0)

);

CREATE INDEX idx_transaction_wallet
    ON transactions(wallet_id);

CREATE INDEX idx_transaction_created_at
    ON transactions(created_at);