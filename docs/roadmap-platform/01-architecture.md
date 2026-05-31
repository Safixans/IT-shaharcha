# Phase 2 — System Architecture

> Builds on `docs/ARCHITECTURE.md` (the platform's source of truth). This
> document specifies the **roadmap ecosystem** as an extension of the existing
> microservice topology, not a new system.

---

## 1. Guiding constraints (inherited from the platform)

- **Database-per-service.** No cross-service SQL joins. Share via REST/Feign or Kafka.
- **Stateless services.** JWT carries identity; gateway injects `X-Account-Id`/`X-Roles`.
- **Clean architecture per service:** controller → service(interface) → impl → repository → entity, with dto/mapper boundaries.
- **Frontend monorepo:** `learner` (:3000), `console` (:3001), `public` (:3002), shared `@itsh/api-client`, `@itsh/auth`.
- **Anonymous-first.** Core roadmap experience requires no login.

---

## 2. Where the roadmap engine lives

The roadmap graph is a **learning concern**, so it extends **`learning-service`**
(it already owns tracks→courses→modules→lessons and the `learning` schema).
Adding it here lets roadmap nodes deep-link to existing catalog rows without a
cross-service hop.

Two new bounded capabilities are introduced as *modules*, not necessarily new
deployables (start in-process, extract later if load demands):

| Capability | Home | Rationale |
|---|---|---|
| **Roadmap graph + progress** | `learning-service` (new package `roadmap/`) | Co-located with catalog it links to |
| **Search** | `learning-service` (Postgres FTS) → extract to `search-service` (Elasticsearch) later | Start simple, scale when needed |
| **AI** | new **`ai-service`** (:9013) | Isolates LLM keys, rate limits, billing, slow calls |

```
                       ┌──────────────────────────────┐
                       │        API Gateway :8080      │  JWT verify, inject headers
                       └───────┬───────────────┬───────┘
             /learning/**      │               │   /ai/**
                   ┌───────────▼──────┐   ┌─────▼─────────┐
                   │ learning-service │   │  ai-service   │  :9013
                   │   :9005          │   │  (new)        │
                   │  catalog +       │   │  path-gen,    │
                   │  roadmap engine +│   │  tutor, gap,  │
                   │  progress + FTS  │   │  quiz-gen     │
                   └───────┬──────────┘   └─────┬─────────┘
                           │ Kafka: roadmap.progress.updated
                           ▼                    │ Feign (read graph + progress)
                  ┌──────────────────┐          │
                  │ analytics-service│◄─────────┘
                  └──────────────────┘
```

### Why a separate `ai-service`
- LLM calls are slow, failure-prone, and key-bearing → must not block catalog requests.
- Independent rate limiting and (future) usage metering.
- Can be scaled/disabled independently; gateway simply 503s `/ai/**` if down.

---

## 3. Roadmap graph engine — runtime design

### 3.1 Authoring vs. rendering split
- **Authoring** (console): create roadmap, add nodes/edges, optionally drag to
  position, attach resources. Validates the graph is a DAG on save.
- **Rendering** (learner): GET roadmap graph JSON → client renders.

### 3.2 Layout strategy (hybrid)
1. **Authored layout:** if every node has `position {x,y}`, render verbatim.
2. **Auto-layout (deterministic):** if positions are null, compute client-side:
   - Topologically sort `SEQUENCE` edges → vertical spine order.
   - Assign `BRANCH` children to alternating left/right gutters.
   - This is the algorithm our current `roadmap/[slug]/page.tsx` already approximates;
     we formalize it into a reusable `@itsh/roadmap-layout` util.
- Auto-layout makes the result deterministic (same input → same picture), which
  matters for SSR/SEO and snapshot tests.

### 3.3 Rendering layer (client)
- **Edges:** one SVG layer (`<path>` per edge) sized to the canvas bounding box.
- **Nodes:** absolutely-positioned HTML (`<button>`/`<div>`) over the SVG →
  accessible, selectable text, SEO-indexable.
- **Why not pure SVG:** SVG text is poor for a11y/SEO/selection; HTML nodes +
  SVG edges is the pragmatic combo.

### 3.4 Validation rules (server, on save)
- No cycles among `SEQUENCE`+`BRANCH` edges (reject with 422).
- Edge endpoints must exist in the same roadmap.
- Node ids unique & slug-safe within a roadmap; immutable after first publish.

---

## 4. Progress architecture

### 4.1 Two-tier (anonymous + authenticated)
- **Anonymous:** `localStorage` keyed `roadmap:{slug}` (extend existing
  `roadmapProgress.ts`) storing `{ nodeId: state }`.
- **Authenticated:** server `roadmap_progress` rows; gateway-authenticated.
- **Merge-on-login:** when an anonymous user logs in, client POSTs local progress
  to `/learning/roadmaps/{slug}/progress/merge`; server union-merges (server wins
  on conflict by `updated_at`), returns canonical set; client clears local-only.

### 4.2 Node progress states
`DONE | IN_PROGRESS | SKIPPED | NONE` (NONE = absent row). Per `(account, node)`.

### 4.3 Eventing
On authenticated progress change → publish `roadmap.progress.updated`
(account, roadmap, node, state) → **analytics-service** updates streaks,
completion %, recommendations; **portfolio** can surface completed roadmaps.

### 4.4 Completion → portfolio loop (our differentiator)
When a roadmap hits 100% (or a defined threshold), emit
`roadmap.completed` → portfolio-service can mint a "roadmap completion"
credential. This is the closed loop roadmap.sh lacks.

---

## 5. Search architecture

### 5.1 Phase A — Postgres FTS (ship first)
- `tsvector` columns over roadmap titles, node titles/summaries, guide text.
- `GIN` index; ranked `websearch_to_tsquery`.
- Endpoint `/learning/search?q=` returns grouped results (roadmaps, nodes, courses).
- Good to ~tens of thousands of docs; zero new infra.

### 5.2 Phase B — Elasticsearch (scale)
- Extract a `search-service`; index via Kafka CDC from learning-service.
- Typo tolerance, synonyms, multi-domain ranking, facets.
- Swap the `/search` gateway route; client unchanged.

### 5.3 Client (⌘K palette)
- Debounced (200ms) query, grouped + keyboard-navigable results, recent searches
  cached locally. Works anonymously.

---

## 6. State management (frontend)

| State kind | Mechanism | Notes |
|---|---|---|
| Server cache (graph, search, progress) | **TanStack Query** (add) | dedupe, caching, optimistic progress |
| Anonymous progress | `localStorage` + small store | existing pattern |
| Auth/session | `@itsh/auth` (existing) | token in localStorage, attached by api-client |
| Drawer / palette / theme | URL params + lightweight context (Zustand or React context) | drawer state = `?topic=` for deep-link |
| Theme (dark/light) | `class` strategy on `<html>` + localStorage + system pref | Tailwind `darkMode: 'class'` |

Principle: **URL is the source of truth** for shareable UI state (selected
roadmap variant, open topic). Ephemeral UI (hover) stays in component state.

---

## 7. AI architecture (`ai-service` :9013)

```
Client → Gateway /ai/** → ai-service
  ├─ POST /ai/paths/generate     (goal, level, time/week) → proposed roadmap graph (draft)
  ├─ POST /ai/gap-analysis       (roadmapId, progress)    → ranked next topics + why
  ├─ POST /ai/tutor/chat         (topicId, question)      → grounded answer (RAG over our content)
  ├─ POST /ai/quiz/generate      (nodeId|courseId)        → quiz questions (→ assessment-service)
  └─ POST /ai/roadmaps/draft     (admin only)             → seed a roadmap an editor then refines
```
- **Grounding/RAG:** retrieve from our own courses/lessons/nodes (Feign →
  learning-service) so answers cite *our* content, reducing hallucination and
  keeping users in-platform.
- **Guardrails:** strict output schemas (JSON), server-side validation before any
  generated graph is persisted; AI never writes directly to catalog — it produces
  *drafts* a human publishes.
- **Provider:** pluggable LLM client; keys only in ai-service config. Per-account
  rate limit via Redis token bucket.

---

## 8. Admin / authoring (console app)

- **Roadmap builder:** canvas with drag-drop nodes, edge-drawing, node inspector
  (title/summary/detail/optional/course link), resource manager.
- **Persistence:** autosave drafts (PATCH), explicit Publish (creates a version).
- **Versioning:** `roadmap_versions` snapshot on publish → rollback + audit.
- **Moderation:** suggestion queue (community/teacher edits) with approve/reject.
- **RBAC:** reuse existing roles — `ROLE_TEACHER` authors, `ROLE_ADMIN` publishes/moderates.

---

## 9. Non-functional architecture

| Concern | Approach |
|---|---|
| Performance | Roadmap graph cached (HTTP cache + TanStack Query); SSR/ISR the public roadmap pages for SEO; SVG edges drawn once |
| Accessibility | HTML nodes, focus trap in drawer, ⌘K keyboard nav, `prefers-reduced-motion`, ARIA roles, color-contrast AA |
| SEO | Server-render `/roadmaps` and `/roadmap/{slug}`; per-roadmap metadata + JSON-LD; topic deep-link URLs |
| Observability | Actuator/Micrometer (existing); trace id through gateway; AI latency + token metrics |
| Security | JWT at edge (existing); AI keys isolated; generated content validated; rate limiting at gateway |
| Scalability | Roadmaps are data → horizontal read scaling; extract search/AI when load demands; CDN for static roadmap pages |
| Resilience | `/ai/**` degrades gracefully (feature hidden if 503); progress works offline (local) |

---

## 10. Component/service inventory (delta to build)

**Backend (learning-service, new `roadmap/` package):** entities (Roadmap,
RoadmapNode, RoadmapEdge, Topic, Resource, RoadmapProgress, RoadmapVersion),
repositories, RoadmapService/ProgressService/SearchService, controllers
(public catalog, progress, admin authoring), MapStruct mappers, Flyway `V3+`.

**Backend (new ai-service):** standard service skeleton + LLM client + RAG
retriever (Feign to learning) + endpoints in §7.

**Frontend (learner):** `@itsh/roadmap-layout` util, `RoadmapCanvas`,
`RoadmapNode`, `EdgeLayer`, `TopicDrawer`, `ResourceList`, `ProgressLegend`,
`CommandPalette`, `ThemeToggle`, dashboard widgets. (Detailed in `04-components.md`.)

**Frontend (console):** `RoadmapBuilder` canvas, `NodeInspector`, `EdgeTool`,
`ResourceManager`, `PublishBar`, moderation queue.

**Shared (`@itsh/api-client`):** roadmap/search/progress/ai methods + types.
