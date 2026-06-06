-- Attachment service: metadata for objects stored in MinIO (bytes live in the bucket).

CREATE TABLE attachments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_account_id UUID         NOT NULL,
    object_key       VARCHAR(512) NOT NULL UNIQUE,
    original_name    VARCHAR(255),
    content_type     VARCHAR(128) NOT NULL,
    size_bytes       BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_attachments_owner ON attachments (owner_account_id);
