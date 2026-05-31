# Phase 3 — Implementation Plan

> Honest framing: the full prompt (interactive visualizer + progress + dashboard
> + profiles + explorer + search + community + notifications + dark mode +
> backend + admin drag-drop builder + 6 AI features) is a **multi-month, team-
> scale** program. It cannot be delivered correctly in one pass, and the standing
> rule here is *nothing is reported done until it's tested and working*. So this
> plan is **incremental and vertically sliced**: every milestone ends with a
> runnable, verified increment that builds on the existing platform — not a pile
> of stubs.

---

## Sequencing principle

Build **back-to-front, thin vertical slices**. Each milestone delivers a usable
feature end-to-end (DB → service → API → api-client → UI → verified) before the
next starts. Ship value early; defer scale (Elasticsearch, multi-deployable AI)
until load justifies it.

---

## Milestone 0 — Foundations (low risk, unblocks everything)
**Goal:** graph data model live; existing roadmaps migrated; nothing user-visible breaks.
- [ ] Flyway `V3__roadmap_engine.sql`, `V4__roadmap_progress.sql` in learning-service.
- [ ] JPA entities + repositories (Roadmap, RoadmapNode, RoadmapEdge, Topic, Resource, RoadmapProgress).
- [ ] Seeder migrating the 4 roadmaps from `lib/roadmaps.ts` (preserve `node_key`).
- [ ] `GET /learning/roadmaps`, `GET /learning/roadmaps/{slug}` (open) + MapStruct DTOs.
- **Done when:** `curl` returns the 4 seeded roadmaps as graph JSON; learning-service
  build + existing tests green; existing localStorage progress keys still valid.

## Milestone 1 — Learner renders from API (replace static file)
**Goal:** the shipped roadmap page renders DB graph via the layout engine.
- [ ] `@itsh/roadmap` package: `autoLayout` + `topoSort` (with cycle detect) + unit tests.
- [ ] `RoadmapCanvas`, `EdgeLayer`, `RoadmapNode` (lift current sticker styles).
- [ ] Rewrite `roadmap/[slug]/page.tsx` to fetch `getRoadmap(slug)` (TanStack Query).
- [ ] Keep localStorage progress working unchanged.
- **Done when:** learner build passes; `/roadmap` + `/roadmap/{slug}` return 200 and
  render identically to today, now data-driven; layout snapshot tests pass.

## Milestone 2 — Topic drawer + typed resources
**Goal:** click a node → right drawer with original explainer + internal-first resources.
- [ ] `topics` + `resources` seeded for existing nodes; `GET /nodes/{nodeKey}`.
- [ ] `TopicDrawer`, `ResourceList`, `ResourceItem`; URL deep-link `?topic=`.
- [ ] Internal resources deep-link to existing courses/lessons; external open safely.
- **Done when:** drawer opens/closes (mouse + keyboard), URL updates, resources link
  correctly, a11y checks pass (focus trap, ESC, ARIA).

## Milestone 3 — Authenticated progress + dashboard
**Goal:** progress syncs across devices; merge-on-login; "my roadmaps".
- [ ] `roadmap_progress` endpoints (`PUT/DELETE/merge`, `GET dashboard`).
- [ ] api-client methods; extend `roadmapProgress.ts` to write-through + sync.
- [ ] `dashboard/page.tsx` progress widgets; emit `roadmap.progress.updated` to Kafka.
- **Done when:** anonymous progress survives login (merge verified), dashboard shows
  correct %, analytics receives the event.

## Milestone 4 — Search (⌘K, Postgres FTS)
**Goal:** global keyboard search across roadmaps/nodes/courses.
- [ ] `V5__roadmap_search_fts.sql` + triggers; `GET /learning/search`.
- [ ] `CommandPalette` (debounced, grouped, keyboard-nav), open anonymously.
- **Done when:** ⌘K finds seeded roadmaps/nodes by title and summary; keyboard-only usable.

## Milestone 5 — Admin roadmap builder (console)
**Goal:** teachers author roadmaps without editing code; admins publish.
- [ ] Admin authoring endpoints (CRUD nodes/edges/resources, positions, publish, versions).
- [ ] Server-side **cycle validation** on publish (422 + offending edges) — tested.
- [ ] `BuilderCanvas` (drag), `NodeInspector`, `EdgeTool`, `ResourceManager`, `PublishBar`.
- [ ] Versioning + rollback; optimistic-lock handling (409).
- **Done when:** create→add nodes/edges→attach resources→publish→appears in learner;
  cycle attempt is rejected with a clear banner; rollback restores a prior version.

## Milestone 6 — Dark mode + polish + responsive/a11y pass
- [ ] `darkMode:'class'` across 3 apps; `ThemeToggle`; dark surfaces defined.
- [ ] Mobile roadmap (single column, optional nodes stack, drawer→bottom sheet).
- [ ] Loading skeletons, reduced-motion, contrast AA audit.
- **Done when:** theme toggles + persists; mobile + desktop verified; Lighthouse a11y ≥ 95.

## Milestone 7 — Community & moderation
- [ ] `POST /roadmaps/{slug}/suggestions`; `SuggestionQueue` approve/reject in console.
- [ ] Notifications hook (reuse notification path) on suggestion status change.
- **Done when:** a learner suggestion appears in the queue and approval applies it.

## Milestone 8 — AI service (phased, opt-in, degrades gracefully)
**Build one feature at a time; each must be schema-validated and never auto-publish.**
- [ ] 8a — `ai-service` skeleton (:9013), gateway route `/ai/**`, Redis rate limit, RAG retriever (Feign → learning).
- [ ] 8b — **Gap analysis** (lowest risk; pure read of progress+graph) → "what next".
- [ ] 8c — **AI tutor chat** (RAG-grounded, cites our content).
- [ ] 8d — **Quiz generation** → optionally persists to assessment-service.
- [ ] 8e — **Path generation** → produces a *draft* roadmap a user saves.
- [ ] 8f — **Admin roadmap draft** → seeds builder for human refinement.
- **Done when:** each feature returns valid schema'd output, is rate-limited, and the
  learner UI hides AI cleanly when `/ai/**` is down (503).

## Milestone 9 — Scale-outs (only when needed)
- [ ] Extract `search-service` + Elasticsearch (Kafka CDC) once FTS strains.
- [ ] SSR/ISR the public roadmap pages + JSON-LD for SEO.
- [ ] Cross-roadmap shared-topic de-dup UI; recommendation engine in analytics.

---

## Cross-cutting acceptance gates (every milestone)
1. Service builds (`mvn -pl <svc> -am package`) and unit/integration tests pass.
2. Affected frontend builds (`npm run build -w @itsh/<app>`); no type errors.
3. Routes verified at HTTP level (200 + expected payload) via the docker stack.
4. No regression in existing routes (smoke the learner/console/public home + auth).
5. Reported to the user **only after** the above pass (per standing testing rule).
   Visual confirmation is flagged as user-side when browser tooling is unavailable.

## Risk register
| Risk | Mitigation |
|---|---|
| Auto-layout produces ugly graphs for complex roadmaps | manual `position` override per node; layout snapshot tests |
| Progress merge data loss | server-wins-by-timestamp, union merge, never destructive delete on merge |
| AI hallucination / unsafe content | strict JSON schemas, RAG grounding, human-publish gate, output filtering |
| Scope creep | vertical slices; each milestone independently shippable |
| Content copyright | original prose only (enforced in `roadmaps.ts` charter); visual pattern reproduction only |

---

## Recommended starting point
**Milestone 0 + 1** together form the smallest safe first PR: move roadmaps into
the database and render the existing page from the API with zero visible change.
It de-risks everything downstream (progress, drawer, builder, AI all depend on the
graph existing in the DB) and is fully testable without any new UX surface.
