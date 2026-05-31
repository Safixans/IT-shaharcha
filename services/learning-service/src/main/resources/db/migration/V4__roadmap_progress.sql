-- Authenticated roadmap progress. Anonymous progress stays in the browser
-- (localStorage); these tables back the cross-device sync added in a later
-- milestone. A missing row means state = NONE.

CREATE TABLE roadmap_progress (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id   UUID NOT NULL,
    roadmap_id   UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    node_id      UUID NOT NULL REFERENCES roadmap_nodes (id) ON DELETE CASCADE,
    state        VARCHAR(16) NOT NULL,            -- done | in_progress | skipped
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_progress UNIQUE (account_id, node_id)
);
CREATE INDEX idx_progress_account_roadmap ON roadmap_progress (account_id, roadmap_id);

CREATE TABLE roadmap_progress_summary (
    account_id      UUID NOT NULL,
    roadmap_id      UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    done_count      INTEGER NOT NULL DEFAULT 0,
    total_count     INTEGER NOT NULL DEFAULT 0,
    percent         DOUBLE PRECISION NOT NULL DEFAULT 0,
    last_active_at  TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    PRIMARY KEY (account_id, roadmap_id)
);
