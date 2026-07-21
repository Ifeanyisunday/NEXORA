-- ==========================================================
-- Migration : V4
-- Description: Create wallets table
-- Project: Nexora Banking API
-- ==========================================================

CREATE TABLE wallets (

    id UUID NOT NULL,

    user_id UUID NOT NULL,

    balance NUMERIC(19, 2) NOT NULL,

    currency VARCHAR(3) NOT NULL,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_wallets
        PRIMARY KEY (id),

    CONSTRAINT uk_wallets_user_id
        UNIQUE (user_id),

    CONSTRAINT fk_wallets_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT chk_wallets_balance
        CHECK (balance >= 0),

    CONSTRAINT chk_wallets_currency
        CHECK (currency IN ('NGN', 'USD', 'EUR', 'GBP')),

    CONSTRAINT chk_wallets_status
        CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))

);