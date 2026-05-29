-- User service schema: profiles and related portfolio entities.

CREATE TABLE profiles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID         NOT NULL UNIQUE,
    full_name   VARCHAR(150),
    avatar_url  VARCHAR(512),
    bio         VARCHAR(1000),
    country     VARCHAR(80),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_profiles_account ON profiles (account_id);

CREATE TABLE education_history (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id     UUID         NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    institution    VARCHAR(200) NOT NULL,
    degree         VARCHAR(120),
    field_of_study VARCHAR(120),
    start_date     DATE,
    end_date       DATE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_education_profile ON education_history (profile_id);

CREATE TABLE certificates (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id     UUID         NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    title          VARCHAR(200) NOT NULL,
    issuer         VARCHAR(200),
    file_id        UUID,
    credential_url VARCHAR(512),
    issued_at      DATE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_certificates_profile ON certificates (profile_id);

CREATE TABLE portfolio_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id  UUID         NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    type        VARCHAR(32)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    url         VARCHAR(512),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_portfolio_profile ON portfolio_items (profile_id);
