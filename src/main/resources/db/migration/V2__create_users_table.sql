-- ==========================================================
-- Migration : V2
-- Description: Create users table
-- Project: Nexora Banking API
-- ==========================================================


CREATE TABLE users (

    id UUID NOT NULL,

    email VARCHAR(255) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    status user_status NOT NULL,

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    failed_login_attempts INTEGER NOT NULL DEFAULT 0,

    account_locked_until TIMESTAMPTZ,

    last_login_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_users
        PRIMARY KEY (id),

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_failed_login_attempts
        CHECK (failed_login_attempts >= 0)

);


COMMENT ON TABLE users IS
'Authentication identities for platform users';

COMMENT ON COLUMN users.email IS
'Unique email address used for login';

COMMENT ON COLUMN users.password_hash IS
'BCrypt password hash';

COMMENT ON COLUMN users.status IS
'Current account status';
