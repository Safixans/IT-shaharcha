-- Assessment service v2 — modular training (IELTS L/R/W, SAT modules, quizzes) + attempts.
-- Replaces the old exam/section/question/session model. Content answer keys are stored as
-- jsonb; attempts snapshot the unit content at start so edits/deactivation can't corrupt them.

CREATE TABLE ielts_units (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    skill                 VARCHAR(16)  NOT NULL,            -- LISTENING / READING / WRITING
    title                 VARCHAR(255) NOT NULL,
    tags                  JSONB        NOT NULL DEFAULT '[]',
    original_section_data TEXT,
    section_data          TEXT,                              -- answer-stripped HTML served to students
    passage               TEXT,
    prompt                TEXT,
    writing_task          VARCHAR(16),                       -- TASK_1 / TASK_2 (writing)
    problems              JSONB,                             -- immutable answer key (null for writing)
    problem_count         INTEGER      NOT NULL DEFAULT 0,
    audio_id              UUID,
    image_id              UUID,
    duration_seconds      INTEGER      NOT NULL DEFAULT 1200,
    active                BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    version               BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_ielts_units_skill ON ielts_units (skill, active);
CREATE INDEX idx_ielts_units_tags  ON ielts_units USING gin (tags);

CREATE TABLE objective_units (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kind             VARCHAR(16)  NOT NULL,                  -- SAT / QUIZ
    sat_section      VARCHAR(24),                            -- READING_WRITING / MATH (SAT)
    title            VARCHAR(255) NOT NULL,
    tags             JSONB        NOT NULL DEFAULT '[]',
    questions        JSONB        NOT NULL DEFAULT '[]',     -- prompts + options(+correct) + key
    problem_count    INTEGER      NOT NULL DEFAULT 0,
    duration_seconds INTEGER      NOT NULL DEFAULT 1800,
    active           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_objective_units_kind ON objective_units (kind, active);
CREATE INDEX idx_objective_units_tags ON objective_units USING gin (tags);

CREATE TABLE attempts (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id            UUID         NOT NULL,
    unit_id               UUID         NOT NULL,
    family                VARCHAR(24)  NOT NULL,
    status                VARCHAR(16)  NOT NULL,
    title                 VARCHAR(255) NOT NULL,
    snapshot_section_data TEXT,
    snapshot_passage      TEXT,
    snapshot_prompt       TEXT,
    snapshot_audio_id     UUID,
    snapshot_image_id     UUID,
    snapshot_problems     JSONB,
    draft_answers         JSONB,
    draft_essay           TEXT,
    answers               JSONB,
    essay                 TEXT,
    correct_count         INTEGER      NOT NULL DEFAULT 0,
    incorrect_count       INTEGER      NOT NULL DEFAULT 0,
    total_count           INTEGER      NOT NULL DEFAULT 0,
    score_percent         DOUBLE PRECISION,
    band                  DOUBLE PRECISION,
    feedback              TEXT,
    criteria              JSONB,
    graded_by             UUID,
    started_at            TIMESTAMPTZ  NOT NULL,
    ends_at               TIMESTAMPTZ  NOT NULL,
    submitted_at          TIMESTAMPTZ,
    graded_at             TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    version               BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_attempts_student        ON attempts (student_id, started_at DESC);
CREATE INDEX idx_attempts_student_family ON attempts (student_id, family, status);
CREATE INDEX idx_attempts_grading        ON attempts (family, status, submitted_at);
