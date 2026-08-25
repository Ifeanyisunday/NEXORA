ALTER TABLE wallets
ADD COLUMN account_number VARCHAR(10);

ALTER TABLE wallets
ADD CONSTRAINT uk_wallet_account_number
UNIQUE (account_number);