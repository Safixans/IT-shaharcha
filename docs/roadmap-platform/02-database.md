# Phase 2 — Database Design

> Extends the `learning` schema (Postgres, per-service DB). Follows the existing
> conventions seen in `services/learning-service/.../db/migration/V1__init_learning.sql`:
> `UUID PK DEFAULT gen_random_uuid()`, `created_at/updated_at TIMESTAMPTZ`,
> `created_by/updated_by VARCHAR(64)`, `deleted BOOLEAN`, `version BIGINT`.
> New migration files: `V3__roadmap_engine.sql`, `V4__roadmap_progress.sql`,
> `V5__roadmap_search_fts.sql`. AI tables live in the separate `ai` schema.

---

## 1. Entity-relationship overview

```
roadmaps ──< roadmap_nodes ──< node_resources >── resources
   │              │   ▲                                │
   │              │   └── topics (shared identity) ────┘
   │              └─< (as from/to) roadmap_edges
   ├──< roadmap_versions        (publish snapshots)
   └──< roadmap_categories (M:N via roadmap_category_map)

roadmap_progress      (account × node → state)        [V4]
roadmap_suggestions   (community edit queue)           [V3]
```

Cardinality summary:
- roadmap **1—N** node, **1—N** edge, **1—N** version
- node **N—1** topic (optional), node **1—N** node_resources
- topic **1—N** resources (shared across roadmaps)
- account **1—N** roadmap_progress

---

## 2. `V3__roadmap_engine.sql` (DDL)

```sql
-- ─────────────────────────────────────────────────────────────
-- Roadmap engine: graph of roadmaps → nodes → edges, shared topics,
-- typed resources, versioning, and a community suggestion queue.
-- ─────────────────────────────────────────────────────────────

CREATE TABLE roadmaps (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug          VARCHAR(160) UNIQUE NOT NULL,
    title         VARCHAR(255) NOT NULL,
    tagline       VARCHAR(500),
    description   TEXT,
    icon          VARCHAR(32),                 -- emoji/glyph for cards
    kind          VARCHAR(16)  NOT NULL DEFAULT 'role',  -- role | skill | exam | career
    status        VARCHAR(16)  NOT NULL DEFAULT 'draft', -- draft | published | archived
    layout_mode   VARCHAR(16)  NOT NULL DEFAULT 'auto',  -- auto | manual
    difficulty    VARCHAR(16),                  -- beginner | intermediate | advanced
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

-- Shared topic identity: "Git", "HTTP" etc. referenced by many roadmaps.
CREATE TABLE topics (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug         VARCHAR(160) UNIQUE NOT NULL,
    title        VARCHAR(255) NOT NULL,
    summary      TEXT,                          -- one-line, original prose
    detail       TEXT,                          -- drawer body, original prose
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(64),
    updated_by   VARCHAR(64),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE roadmap_nodes (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id   UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    node_key     VARCHAR(160) NOT NULL,         -- stable id within roadmap (progress key)
    type         VARCHAR(20)  NOT NULL DEFAULT 'topic', -- topic|subtopic|section|milestone|paragraph
    title        VARCHAR(255) NOT NULL,
    summary      TEXT,                          -- original prose
    detail       TEXT,                          -- original prose
    optional     BOOLEAN      NOT NULL DEFAULT FALSE,
    topic_id     UUID REFERENCES topics (id) ON DELETE SET NULL, -- optional shared identity
    course_id    UUID,                          -- deep-link into learning.courses (same DB)
    lesson_id    UUID,                          -- optional deep-link into learning.lessons
    pos_x        DOUBLE PRECISION,              -- null ⇒ auto-layout
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
CREATE INDEX idx_nodes_topic   ON roadmap_nodes (topic_id);

CREATE TABLE roadmap_edges (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id   UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    from_node_id UUID NOT NULL REFERENCES roadmap_nodes (id) ON DELETE CASCADE,
    to_node_id   UUID NOT NULL REFERENCES roadmap_nodes (id) ON DELETE CASCADE,
    kind         VARCHAR(16)  NOT NULL DEFAULT 'sequence', -- sequence | branch | related
    style        VARCHAR(16)  NOT NULL DEFAULT 'solid',    -- solid | dotted
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_edge UNIQUE (roadmap_id, from_node_id, to_node_id, kind),
    CONSTRAINT chk_no_self_loop CHECK (from_node_id <> to_node_id)
);
CREATE INDEX idx_edges_roadmap ON roadmap_edges (roadmap_id);
CREATE INDEX idx_edges_from     ON roadmap_edges (from_node_id);
CREATE INDEX idx_edges_to       ON roadmap_edges (to_node_id);

-- Typed learning resources, owned by a topic (shared) — internal-first.
CREATE TABLE resources (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id     UUID NOT NULL REFERENCES topics (id) ON DELETE CASCADE,
    type         VARCHAR(20)  NOT NULL,         -- article|video|official_docs|course|lesson|feed|opensource|exam
    title        VARCHAR(300) NOT NULL,
    url          TEXT,                          -- external link (nullable if internal_ref set)
    internal_ref UUID,                          -- course/lesson/exam id when internal
    is_official  BOOLEAN      NOT NULL DEFAULT FALSE,
    is_premium   BOOLEAN      NOT NULL DEFAULT FALSE,
    votes        INTEGER      NOT NULL DEFAULT 0,
    order_index  INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(64),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    version      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_resource_target CHECK (url IS NOT NULL OR internal_ref IS NOT NULL)
);
CREATE INDEX idx_resources_topic ON resources (topic_id);

-- Per-node resource overrides/extras (resource not in shared topic set).
CREATE TABLE node_resources (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id      UUID NOT NULL REFERENCES roadmap_nodes (id) ON DELETE CASCADE,
    resource_id  UUID NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
    order_index  INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_node_resource UNIQUE (node_id, resource_id)
);

-- Categories for the directory (Frontend/Backend/Data/AI…).
CREATE TABLE roadmap_categories (
    id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug   VARCHAR(120) UNIQUE NOT NULL,
    title  VARCHAR(160) NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE roadmap_category_map (
    roadmap_id  UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES roadmap_categories (id) ON DELETE CASCADE,
    PRIMARY KEY (roadmap_id, category_id)
);

-- Publish snapshots → rollback + audit. Full graph frozen as JSONB.
CREATE TABLE roadmap_versions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id    UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    version_no    INTEGER NOT NULL,
    graph_json    JSONB   NOT NULL,             -- {nodes, edges, meta} at publish time
    published_by  VARCHAR(64),
    published_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_roadmap_version UNIQUE (roadmap_id, version_no)
);

-- Community/teacher edit suggestions awaiting moderation.
CREATE TABLE roadmap_suggestions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id    UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    node_id       UUID REFERENCES roadmap_nodes (id) ON DELETE SET NULL,
    account_id    UUID NOT NULL,
    kind          VARCHAR(20) NOT NULL,         -- add_resource | edit_text | report | new_node
    payload       JSONB NOT NULL,
    status        VARCHAR(16) NOT NULL DEFAULT 'pending', -- pending|approved|rejected
    reviewed_by   VARCHAR(64),
    reviewed_at   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_suggestions_status ON roadmap_suggestions (status);
```

### Cycle protection
Cycle detection is enforced **in the service layer** on save (topological sort of
`sequence`+`branch` edges; reject 422 on cycle). Postgres cannot cheaply express
"acyclic" as a constraint, so this is an application invariant, tested.

---

## 3. `V4__roadmap_progress.sql`

```sql
CREATE TABLE roadmap_progress (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id   UUID NOT NULL,                 -- from JWT (X-Account-Id)
    roadmap_id   UUID NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    node_id      UUID NOT NULL REFERENCES roadmap_nodes (id) ON DELETE CASCADE,
    state        VARCHAR(16) NOT NULL,          -- done | in_progress | skipped
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_progress UNIQUE (account_id, node_id)
);
CREATE INDEX idx_progress_account_roadmap ON roadmap_progress (account_id, roadmap_id);

-- Denormalized roll-up for fast dashboard reads (kept current by trigger/service).
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
```
- `state = NONE` is represented by **absence** of a row (delete on un-toggle).
- Summary updated in the same transaction as a progress write (service layer),
  so the learning dashboard reads one row per roadmap, not an aggregate scan.

---

## 4. `V5__roadmap_search_fts.sql` (Phase A search)

```sql
ALTER TABLE roadmaps      ADD COLUMN search_tsv tsvector;
ALTER TABLE roadmap_nodes ADD COLUMN search_tsv tsvector;
ALTER TABLE topics        ADD COLUMN search_tsv tsvector;

CREATE INDEX idx_roadmaps_tsv ON roadmaps      USING GIN (search_tsv);
CREATE INDEX idx_nodes_tsv    ON roadmap_nodes USING GIN (search_tsv);
CREATE INDEX idx_topics_tsv   ON topics        USING GIN (search_tsv);

-- Maintain tsv via triggers (title weighted A, summary/detail B/C).
CREATE FUNCTION roadmaps_tsv_update() RETURNS trigger AS $$
BEGIN
  NEW.search_tsv :=
      setweight(to_tsvector('english', coalesce(NEW.title,'')), 'A') ||
      setweight(to_tsvector('english', coalesce(NEW.tagline,'')), 'B') ||
      setweight(to_tsvector('english', coalesce(NEW.description,'')), 'C');
  RETURN NEW;
END $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_roadmaps_tsv BEFORE INSERT OR UPDATE ON roadmaps
  FOR EACH ROW EXECUTE FUNCTION roadmaps_tsv_update();
-- analogous triggers for roadmap_nodes and topics
```

Query: `websearch_to_tsquery('english', :q)` ranked by `ts_rank` + weight, then
unioned and grouped by type in the service layer.

---

## 5. `ai` schema (ai-service, separate DB)

```sql
CREATE TABLE ai_generations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id    UUID,
    feature       VARCHAR(24) NOT NULL,   -- path_gen|gap|tutor|quiz|draft
    input_json    JSONB NOT NULL,
    output_json   JSONB,
    model         VARCHAR(64),
    tokens_in     INTEGER,
    tokens_out    INTEGER,
    status        VARCHAR(16) NOT NULL DEFAULT 'ok', -- ok|error|filtered
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ai_gen_account ON ai_generations (account_id, created_at);

CREATE TABLE ai_rate_buckets (   -- backed by Redis at runtime; table = audit/fallback
    account_id    UUID PRIMARY KEY,
    window_start  TIMESTAMPTZ NOT NULL,
    count         INTEGER NOT NULL DEFAULT 0
);
```
AI-generated roadmaps are **never** written to catalog tables directly; they land
as `roadmap_suggestions(kind='new_node'|...)` or as a `draft` roadmap a human
publishes.

---

## 6. Indexing & performance notes

- Hot read = "render roadmap by slug": one `roadmaps` row + nodes + edges +
  resource counts. Covered by `idx_nodes_roadmap`, `idx_edges_roadmap`.
- Dashboard = `roadmap_progress_summary` by `account_id` (PK prefix scan).
- Search = three GIN indexes; fine to ~10⁴–10⁵ rows before Elasticsearch.
- All large tables carry `deleted` → partial indexes `WHERE deleted = FALSE` on
  the high-traffic filters.

---

## 7. Seeding / migration of existing data

The current 4 roadmaps in `frontend/apps/learner/lib/roadmaps.ts` are migrated by
a one-off seeder (`V6__seed_roadmaps.sql` or a `CommandLineRunner`) that inserts
each roadmap, its nodes (`node_key` = existing `id`), `sequence` edges between
consecutive non-optional steps, and `branch` edges for `optional` steps. **Node
keys are preserved**, so any progress already stored in localStorage stays valid
after the cutover.
