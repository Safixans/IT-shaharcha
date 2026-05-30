-- Assessment service schema (specs/assessment.openapi.yaml).
-- Catalog: exams -> sections -> questions. Plus exam sessions, the answers saved
-- during a session, and the scored results. JSON-shaped fields (choices,
-- correctAnswer, answer value, sectionScores) are stored as TEXT (see the
-- AttributeConverters in com.itshaharcha.assessment.convert).

CREATE TABLE exams (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(255) NOT NULL,
    exam_type         VARCHAR(16)  NOT NULL,
    description       TEXT,
    duration_minutes  INTEGER,
    is_real_exam      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_exams_type ON exams (exam_type);

CREATE TABLE sections (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id           UUID         NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    name              VARCHAR(255) NOT NULL,
    order_index       INTEGER      NOT NULL DEFAULT 0,
    duration_minutes  INTEGER,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    version           BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_sections_exam ON sections (exam_id);

CREATE TABLE questions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id     UUID         NOT NULL REFERENCES sections (id) ON DELETE CASCADE,
    exam_id        UUID         NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    prompt         TEXT         NOT NULL,
    kind           VARCHAR(16)  NOT NULL,
    order_index    INTEGER      NOT NULL DEFAULT 0,
    points         DOUBLE PRECISION NOT NULL DEFAULT 1,
    choices        TEXT,
    correct_answer TEXT,
    explanation    TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_questions_section ON questions (section_id);
CREATE INDEX idx_questions_exam    ON questions (exam_id);

CREATE TABLE exam_sessions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id       UUID         NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    account_id    UUID         NOT NULL,
    status        VARCHAR(16)  NOT NULL DEFAULT 'in_progress',
    started_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at    TIMESTAMPTZ,
    submitted_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(64),
    updated_by    VARCHAR(64),
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    version       BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_exam_sessions_account ON exam_sessions (account_id);
CREATE INDEX idx_exam_sessions_exam    ON exam_sessions (exam_id, account_id);

CREATE TABLE session_answers (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id   UUID         NOT NULL REFERENCES exam_sessions (id) ON DELETE CASCADE,
    question_id  UUID         NOT NULL,
    section_id   UUID,
    value        TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(64),
    updated_by   VARCHAR(64),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (session_id, question_id)
);

CREATE INDEX idx_session_answers_session ON session_answers (session_id);

CREATE TABLE exam_results (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id     UUID         NOT NULL UNIQUE REFERENCES exam_sessions (id) ON DELETE CASCADE,
    exam_id        UUID         NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    account_id     UUID         NOT NULL,
    exam_type      VARCHAR(16)  NOT NULL,
    scaled_score   DOUBLE PRECISION NOT NULL,
    max_score      DOUBLE PRECISION,
    section_scores TEXT,
    scored_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    feedback       TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_exam_results_account ON exam_results (account_id, scored_at);
CREATE INDEX idx_exam_results_type    ON exam_results (account_id, exam_type);
