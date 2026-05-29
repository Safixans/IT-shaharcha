-- Identity service schema: accounts, roles, tokens, OTP codes, profiles.
-- Owns the full identity domain (specs/identity.openapi.yaml).

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(64)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE accounts (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(255) NOT NULL UNIQUE,
    username       VARCHAR(64)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255),
    status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    provider       VARCHAR(16)  NOT NULL DEFAULT 'LOCAL',
    provider_id    VARCHAR(255),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_accounts_email    ON accounts (lower(email));
CREATE INDEX idx_accounts_username ON accounts (lower(username));
CREATE INDEX idx_accounts_status   ON accounts (status);
CREATE INDEX idx_accounts_provider ON accounts (provider, provider_id);

CREATE TABLE account_roles (
    account_id UUID NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    role_id    UUID NOT NULL REFERENCES roles (id)    ON DELETE CASCADE,
    PRIMARY KEY (account_id, role_id)
);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_refresh_tokens_account ON refresh_tokens (account_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);

CREATE TABLE otp_codes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    code_hash   VARCHAR(64)  NOT NULL,
    purpose     VARCHAR(32)  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    consumed    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_otp_codes_account ON otp_codes (account_id, purpose);

CREATE TABLE profiles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID         NOT NULL UNIQUE REFERENCES accounts (id) ON DELETE CASCADE,
    full_name   VARCHAR(120),
    bio         VARCHAR(500),
    avatar_url  VARCHAR(512),
    locale      VARCHAR(16),
    country     VARCHAR(80),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE profile_links (
    profile_id UUID         NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    position   INTEGER      NOT NULL,
    label      VARCHAR(80)  NOT NULL,
    url        VARCHAR(512) NOT NULL,
    PRIMARY KEY (profile_id, position)
);

-- Seed the default role catalog.
INSERT INTO roles (name, description, created_by) VALUES
    ('ROLE_STUDENT', 'Default learner role.',        'system'),
    ('ROLE_TEACHER', 'Content authoring role.',      'system'),
    ('ROLE_ADMIN',   'Full administrative access.',  'system');
