CREATE INDEX idx_transaction_wallet_created_id
ON transactions (
    wallet_id,
    created_at DESC,
    id DESC
);