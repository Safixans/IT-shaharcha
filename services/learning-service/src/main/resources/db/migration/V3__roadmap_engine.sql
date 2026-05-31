-- Roadmap engine: a graph of roadmaps -> nodes -> edges. Nodes carry original
-- prose (summary/detail) and may deep-link to a course. Edges encode the spine
-- (sequence) and optional branches. See docs/roadmap-platform/02-database.md.

CREATE TABLE roadmaps (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug          VARCHAR(160) UNIQUE NOT NULL,
    title         VARCHAR(255) NOT NULL,
    tagline       VARCHAR(500),
    description   TEXT,
    icon          VARCHAR(32),
    kind          VARCHAR(16)  NOT NULL DEFAULT 'role',   -- role | skill | exam | career
    status        VARCHAR(16)  NOT NULL DEFAULT 'draft',  -- draft | published | archived
    layout_mode   VARCHAR(16)  NOT NULL DEFAULT 'auto',   -- auto | manual
    difficulty    VARCHAR(16),                            -- beginner | intermediate | advanced
    est_hours     INTEGER,
    published_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(64),
    updated_by    VARCHAR(64),
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    version       BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_roadmaps_status ON roadmaps (status) WHERE deleted = FALSE;
CREATE INDEX idx_roadmaps_kind   ON roadmaps (kind);

CREATE TABLE roadmap_nodes (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id   UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    node_key     VARCHAR(160) NOT NULL,                  -- stable id within roadmap (progress key)
    type         VARCHAR(20)  NOT NULL DEFAULT 'topic',  -- topic | section | milestone | paragraph
    title        VARCHAR(255) NOT NULL,
    summary      TEXT,
    detail       TEXT,
    optional     BOOLEAN      NOT NULL DEFAULT FALSE,
    course_id    UUID,                                   -- deep-link into learning.courses (same DB)
    pos_x        DOUBLE PRECISION,                       -- null => client auto-layout
    pos_y        DOUBLE PRECISION,
    order_index  INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(64),
    updated_by   VARCHAR(64),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_node_key UNIQUE (roadmap_id, node_key)
);
CREATE INDEX idx_nodes_roadmap ON roadmap_nodes (roadmap_id);

CREATE TABLE roadmap_edges (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id   UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    from_node_id UUID NOT NULL REFERENCES roadmap_nodes (id) ON DELETE CASCADE,
    to_node_id   UUID NOT NULL REFERENCES roadmap_nodes (id) ON DELETE CASCADE,
    kind         VARCHAR(16)  NOT NULL DEFAULT 'sequence', -- sequence | branch | related
    style        VARCHAR(16)  NOT NULL DEFAULT 'solid',    -- solid | dotted
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(64),
    updated_by   VARCHAR(64),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_edge UNIQUE (roadmap_id, from_node_id, to_node_id, kind),
    CONSTRAINT chk_no_self_loop CHECK (from_node_id <> to_node_id)
);
CREATE INDEX idx_edges_roadmap ON roadmap_edges (roadmap_id);
CREATE INDEX idx_edges_from    ON roadmap_edges (from_node_id);
CREATE INDEX idx_edges_to      ON roadmap_edges (to_node_id);
