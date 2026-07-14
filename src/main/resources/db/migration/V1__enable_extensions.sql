-- ==========================================================
-- Migration : V1
-- Description: Enable PostgreSQL extensions
-- Project: Nexora Banking API
-- ==========================================================

-- Generate cryptographically secure UUIDs
CREATE EXTENSION IF NOT EXISTS pgcrypto;