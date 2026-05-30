-- Analytics service schema (specs/analytics.openapi.yaml).
-- The cross-domain aggregator: every accepted DomainEvent is persisted (processed_events,
-- deduped on event_id), folded into per-account/per-domain rollups (domain_progress), and
-- may award a milestone (milestones). Rankings/dashboards are computed from these tables.

CREATE TABLE processed_events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id      UUID         NOT NULL UNIQUE,
    type          VARCHAR(64)  NOT NULL,
    source        VARCHAR(32)  NOT NULL,
    account_id    UUID         NOT NULL,
    occurred_at   TIMESTAMPTZ  NOT NULL,
    recorded_at   TIMESTAMPTZ,
    subject_type  VARCHAR(64),
    subject_id    VARCHAR(128),
    roles_json    TEXT,
    data_json     TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(64),
    updated_by    VARCHAR(64),
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    version       BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_processed_events_account ON processed_events (account_id, occurred_at DESC);
CREATE INDEX idx_processed_events_source  ON processed_events (source);

CREATE TABLE domain_progress (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id       UUID         NOT NULL,
    domain           VARCHAR(32)  NOT NULL,
    points           BIGINT       NOT NULL DEFAULT 0,
    level            INTEGER,
    last_activity_at TIMESTAMPTZ,
    counters         TEXT         NOT NULL DEFAULT '{}',
    streak_days      INTEGER,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_domain_progress_account_domain UNIQUE (account_id, domain)
);

CREATE INDEX idx_domain_progress_account ON domain_progress (account_id);
CREATE INDEX idx_domain_progress_board   ON domain_progress (domain, points DESC);

CREATE TABLE milestones (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id   UUID         NOT NULL,
    milestone    VARCHAR(64)  NOT NULL,
    points       INTEGER      NOT NULL DEFAULT 0,
    domain       VARCHAR(32),
    reached_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(64),
    updated_by   VARCHAR(64),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_milestones_account_milestone UNIQUE (account_id, milestone)
);

CREATE INDEX idx_milestones_account ON milestones (account_id, reached_at DESC);
