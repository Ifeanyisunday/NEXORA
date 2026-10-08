CREATE TABLE fundings (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,
    wallet_id UUID NOT NULL,

    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,

    reference VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,

    provider VARCHAR(30) NOT NULL,
    provider_reference VARCHAR(150),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    completed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uk_funding_reference
        UNIQUE (reference),

    CONSTRAINT uk_funding_provider_reference
        UNIQUE (provider, provider_reference),

    CONSTRAINT fk_funding_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT fk_funding_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets (id)
);

CREATE INDEX idx_funding_user_created
ON fundings (user_id, created_at);

CREATE INDEX idx_funding_wallet_created
ON fundings (wallet_id, created_at);

CREATE INDEX idx_funding_status
ON fundings (status);