-- ==========================================================
-- Migration : V2
-- Description: Create users status type
-- Project: Nexora Banking API
-- ==========================================================

CREATE TYPE user_status AS ENUM (
    'ACTIVE',
    'LOCKED',
    'DISABLED',
    'PENDING_VERIFICATION'
);