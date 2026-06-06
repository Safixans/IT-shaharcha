-- Groups & teacher↔student membership (independent of the account/role domain).
-- A student belongs to at most one group (UNIQUE student_id). Groups belong to a teacher.

INSERT INTO roles (name, description, created_by) VALUES
    ('ROLE_MODERATOR', 'Manages groups and their memberships.', 'system')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE study_groups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(120) NOT NULL,
    teacher_id  UUID         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_study_groups_teacher ON study_groups (teacher_id);

CREATE TABLE group_memberships (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id    UUID         NOT NULL REFERENCES study_groups (id) ON DELETE CASCADE,
    -- One group per student: enforced here AND in the service (409 on violation).
    student_id  UUID         NOT NULL UNIQUE REFERENCES accounts (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(64),
    updated_by  VARCHAR(64),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_group_memberships_group ON group_memberships (group_id);
