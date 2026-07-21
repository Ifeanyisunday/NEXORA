-- ==========================================================
-- Migration : V4
-- Description: Create transfers table
-- Project: Nexora Banking API
-- ==========================================================

CREATE TABLE transfers (

    id UUID NOT NULL,

    sender_id UUID NOT NULL,

    receiver_id UUID NOT NULL,

    amount NUMERIC(19, 2) NOT NULL,

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_transfers
        PRIMARY KEY (id),

    CONSTRAINT fk_transfers_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id),

    CONSTRAINT fk_transfers_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES users(id),

    CONSTRAINT chk_transfers_amount
        CHECK (amount > 0)

);