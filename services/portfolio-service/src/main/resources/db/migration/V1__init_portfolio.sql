-- Portfolio service schema (specs/portfolio.openapi.yaml).
-- Uploaded file blobs, certificates (+ verification), education history, free-form
-- portfolio items (tags stored as JSON text via StringListConverter), and the
-- per-account publish state for the assembled academic portfolio.

CREATE TABLE files (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id     UUID         NOT NULL,
    original_name  VARCHAR(255),
    content_type   VARCHAR(128) NOT NULL,
    size_bytes     BIGINT       NOT NULL DEFAULT 0,
    data           BYTEA,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_files_account ON files (account_id);

CREATE TABLE certificates (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id        UUID         NOT NULL,
    title             VARCHAR(255) NOT NULL,
    issuer            VARCHAR(255),
    issued_on         DATE,
    file_id           UUID,
    credential_url    VARCHAR(1024),
    status            VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    verified_at       TIMESTAMPTZ,
    verification_note TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_certificates_account ON certificates (account_id, created_at);

CREATE TABLE education (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id      UUID         NOT NULL,
    institution     VARCHAR(255) NOT NULL,
    degree          VARCHAR(255),
    field_of_study  VARCHAR(255),
    start_date      DATE,
    end_date        DATE,
    description     TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_education_account ON education (account_id);

CREATE TABLE portfolio_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id   UUID         NOT NULL,
    kind         VARCHAR(16)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    url          VARCHAR(1024),
    file_id      UUID,
    tags         TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(64),
    updated_by   VARCHAR(64),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_portfolio_items_account ON portfolio_items (account_id, kind);

CREATE TABLE portfolios (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id    UUID         NOT NULL UNIQUE,
    handle        VARCHAR(128) UNIQUE,
    visibility    VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE',
    published_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(64),
    updated_by    VARCHAR(64),
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    version       BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_portfolios_handle ON portfolios (handle);
