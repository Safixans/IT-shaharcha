-- Learning service schema (specs/learning.openapi.yaml).
-- Catalog: tracks -> courses -> modules -> lessons. Plus tutorials, docs,
-- typing practice, learner progress, and external content sources (feeds).

CREATE TABLE tracks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE courses (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    track_id          UUID REFERENCES tracks (id) ON DELETE SET NULL,
    title             VARCHAR(255) NOT NULL,
    slug              VARCHAR(255) UNIQUE,
    summary           TEXT,
    level             VARCHAR(16)  NOT NULL DEFAULT 'beginner',
    estimated_minutes INTEGER,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_courses_track ON courses (track_id);
CREATE INDEX idx_courses_level ON courses (level);

CREATE TABLE modules (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id   UUID         NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    order_index INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_modules_course ON modules (course_id);

CREATE TABLE lessons (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id         UUID         NOT NULL REFERENCES modules (id) ON DELETE CASCADE,
    title             VARCHAR(255) NOT NULL,
    order_index       INTEGER      NOT NULL DEFAULT 0,
    kind              VARCHAR(16)  NOT NULL DEFAULT 'reading',
    estimated_minutes INTEGER,
    body              TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_lessons_module ON lessons (module_id);

CREATE TABLE enrollments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id        UUID         NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    account_id       UUID         NOT NULL,
    status           VARCHAR(16)  NOT NULL DEFAULT 'active',
    progress_percent DOUBLE PRECISION NOT NULL DEFAULT 0,
    enrolled_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (course_id, account_id)
);

CREATE INDEX idx_enrollments_account ON enrollments (account_id);

CREATE TABLE lesson_progress (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id               UUID         NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    course_id               UUID         NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    account_id              UUID         NOT NULL,
    completed               BOOLEAN      NOT NULL DEFAULT FALSE,
    score_percent           DOUBLE PRECISION,
    started_at              TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by              VARCHAR(64),
    updated_by              VARCHAR(64),
    deleted                 BOOLEAN      NOT NULL DEFAULT FALSE,
    version                 BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (lesson_id, account_id)
);

CREATE INDEX idx_lesson_progress_account ON lesson_progress (account_id);
CREATE INDEX idx_lesson_progress_course  ON lesson_progress (course_id, account_id);

CREATE TABLE content_sources (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255) NOT NULL,
    type           VARCHAR(16)  NOT NULL,
    target         VARCHAR(16)  NOT NULL,
    url            VARCHAR(2048) NOT NULL,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    schedule       VARCHAR(64),
    default_topic  VARCHAR(255),
    status         VARCHAR(16)  NOT NULL DEFAULT 'active',
    last_synced_at TIMESTAMPTZ,
    last_error     TEXT,
    item_count     INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_content_sources_type ON content_sources (type);

CREATE TABLE tutorials (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(255) NOT NULL,
    topic            VARCHAR(255),
    video_url        VARCHAR(2048) NOT NULL,
    duration_seconds INTEGER,
    thumbnail_url    VARCHAR(2048),
    source_id        UUID REFERENCES content_sources (id) ON DELETE SET NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_tutorials_topic  ON tutorials (topic);
CREATE INDEX idx_tutorials_source ON tutorials (source_id);

CREATE TABLE tutorial_watches (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tutorial_id      UUID         NOT NULL REFERENCES tutorials (id) ON DELETE CASCADE,
    account_id       UUID         NOT NULL,
    watched_seconds  INTEGER      NOT NULL DEFAULT 0,
    position_seconds INTEGER,
    completed        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (tutorial_id, account_id)
);

CREATE INDEX idx_tutorial_watches_account ON tutorial_watches (account_id);

CREATE TABLE docs (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(255) NOT NULL,
    topic             VARCHAR(255),
    url               VARCHAR(2048),
    body              TEXT,
    estimated_minutes INTEGER,
    source_id         UUID REFERENCES content_sources (id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_docs_topic  ON docs (topic);
CREATE INDEX idx_docs_source ON docs (source_id);

CREATE TABLE doc_reads (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doc_id           UUID         NOT NULL REFERENCES docs (id) ON DELETE CASCADE,
    account_id       UUID         NOT NULL,
    duration_seconds INTEGER,
    scroll_percent   DOUBLE PRECISION,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (doc_id, account_id)
);

CREATE INDEX idx_doc_reads_account ON doc_reads (account_id);

CREATE TABLE typing_lessons (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    difficulty  VARCHAR(16),
    text        TEXT         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE typing_sessions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id       UUID         NOT NULL,
    lesson_id        UUID REFERENCES typing_lessons (id) ON DELETE SET NULL,
    wpm              DOUBLE PRECISION NOT NULL,
    accuracy_percent DOUBLE PRECISION NOT NULL,
    duration_seconds INTEGER      NOT NULL,
    keystrokes       INTEGER,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_typing_sessions_account ON typing_sessions (account_id, created_at);

CREATE TABLE source_sync_runs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id      UUID         NOT NULL REFERENCES content_sources (id) ON DELETE CASCADE,
    status         VARCHAR(16)  NOT NULL DEFAULT 'queued',
    started_at     TIMESTAMPTZ,
    items_imported INTEGER,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_source_sync_runs_source ON source_sync_runs (source_id);
