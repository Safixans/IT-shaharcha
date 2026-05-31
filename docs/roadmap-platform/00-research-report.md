# Phase 1 — Reverse-Engineering Research Report: roadmap.sh

> **Scope & honesty note.** A live, instrumented audit of roadmap.sh was not
> possible from this environment (no browser access). This report is built from
> detailed working knowledge of the site plus first-principles design-systems
> reasoning. Where a value is a *design decision we are adopting* rather than a
> *measurement of theirs*, it is marked **[ours]**. Treat exact pixel/hex values
> as faithful reconstructions to verify in-browser, not certified measurements.
>
> **Goal of this document:** understand *why* roadmap.sh works, so IT-Shaharcha
> can reproduce the experience — not the content. All curriculum text in our
> product stays original (see `frontend/apps/learner/lib/roadmaps.ts` header).

---

## 0. Executive summary — what makes roadmap.sh work

roadmap.sh is, at its core, **a directed graph of skills rendered as a single
tall flowchart**, wrapped in a content/SEO machine and a light progress layer.
Its success comes from four decisions:

1. **One canonical artifact per career.** Each role (Frontend, Backend, DevOps…)
   is *one* opinionated, visually dense roadmap. There is no paralysis of choice
   inside a roadmap — you scroll a spine top-to-bottom.
2. **The graph is the product.** The flowchart is not decoration; it encodes
   prerequisite ordering, "core vs optional", and alternatives. Reading order =
   learning order.
3. **Progress is frictionless.** Click a node → mark Done/In-progress/Skipped.
   Stored locally first, synced if logged in. No course enrollment ceremony.
4. **Content scales without redesign.** Roadmaps are *data* (originally hand-laid
   in a JSON/editor format), so hundreds of roadmaps, guides, and "best
   practices" pages share three or four page templates.

Everything below decomposes these into reproducible specifications.

---

## 1. Information Architecture

### 1.1 Sitemap (observed top-level structure)

```
/                                  Home — grid of roadmap cards by category
/roadmaps                          Directory of all role/skill roadmaps
/{slug}                            A single role roadmap (e.g. /frontend, /backend)
/{slug}?r={variant}                Variant rendering of a roadmap
/best-practices/{slug}             Opinionated checklists (e.g. backend-performance)
/guides/{slug}                     Long-form articles (SEO surface)
/videos/{slug}                     Video index
/questions/{slug}                  Q&A / interview question decks
/ai  /ai-tutor                     AI roadmap & tutor surface
/teams                             B2B: private roadmaps for orgs
/account/*                         Auth'd: progress, favorites, settings
/{slug}/{topic-slug}               Topic drawer deep-link (resource panel)
```

### 1.2 Navigation hierarchy

- **Primary nav (sticky top):** logo · Roadmaps · Best Practices · Guides ·
  Videos · (AI) · auth actions (Login / Sign up / avatar).
- **Secondary, contextual:** inside a roadmap — a slim toolbar (Download,
  Share, Mark progress legend, "Suggest changes", login-to-track CTA).
- **Tertiary:** the **topic drawer** — a right-hand slide-over opened by
  clicking a node; contains the topic description + curated resource links +
  progress controls. This is the real workhorse of the UX.

### 1.3 Primary user journeys

| Journey | Path | Emotional job |
|---|---|---|
| "Where do I start?" | Home → pick role card → roadmap | Reduce overwhelm |
| "What's next for me?" | (logged in) → roadmap with progress restored | Continuity |
| "What is X and how do I learn it?" | node click → drawer → resources | Just-in-time depth |
| "Prove/track my growth" | mark nodes → progress % persists | Motivation |
| "Discover adjacent skills" | roadmap → linked roadmaps / guides | Exploration |
| SEO entry | Google → /guides/* or /{slug} → explore | Acquisition |

### 1.4 Content relationships (the knowledge graph)

```
Domain (Web Dev) ─┬─ Roadmap (Frontend)
                  │     ├─ Node (HTML)         [core]
                  │     │    └─ Resource[]      (article/video/opensource/feed)
                  │     ├─ Node (CSS)          [core]
                  │     ├─ Node (Tailwind)     [optional, child-of CSS]
                  │     ├─ Edge HTML → CSS     (prerequisite/sequence)
                  │     └─ Milestone (Build a page)
                  └─ Roadmap (Backend) … (cross-links: shared nodes like Git, HTTP)
```

Key relationship types we must model:
- **Sequence edges** (do A before B) — the visual spine.
- **Branch edges** (optional/alternative off a spine node) — dotted connectors.
- **Cross-roadmap references** (Git appears in many) — shared topic identity.
- **Resource attachment** (node → many curated links, typed & rated).

### 1.5 Topic categorization system

roadmap.sh buckets roadmaps into a few **categories** on the home/directory:
*Role-based* (Frontend, Backend, DevOps, Full Stack, Android, …) and
*Skill-based* (React, SQL, Docker, …), plus emerging clusters (AI, Data).
Categorization is shallow (1–2 levels) on purpose — discoverability beats
taxonomy depth.

### 1.6 How content scales across hundreds of pages

- Roadmaps, guides, best-practices are **content-as-data / content-as-files**,
  not bespoke pages. A handful of templates render everything.
- Each node has a **stable id**; progress and resources key off it, so editing
  copy never breaks a user's saved progress.
- SEO long-tail (`/guides/*`, `/questions/*`) is generated from the same store,
  giving compounding organic acquisition with near-zero marginal page cost.

**Takeaway for us:** model roadmaps as graph *data* in `learning-service`, render
through 3–4 templates, key everything on stable node ids. This is the single most
important structural lesson.

---

## 2. Design System Analysis

> Reconstructed values. Our adopted system already lives in
> `frontend/apps/*/tailwind.config.ts` + `globals.css`; we reconcile below.

### 2.1 Typography
- **Family:** humanist sans (roadmap.sh uses a Balsamiq-style hand-drawn font
  *inside* the flowchart boxes to signal "sketch / not final", and a clean sans
  for chrome). **[ours]** We keep **Inter** for chrome and may add one
  "sketch" display face *only* for roadmap node labels to echo the playful feel.
- **Scale (chrome):** ~12 / 14 / 16 / 18 / 24 / 30 / 36 / 48 px. Body 16,
  small 14, node labels ~14–15 semibold.
- **Weights:** 400 body, 500 medium (nav/labels), 600–700 headings.
- **Line height:** ~1.5 body, ~1.2 headings, ~1.15 node labels.

### 2.2 Spacing system
- 4px base unit; common steps 4/8/12/16/20/24/32/48/64.
- Roadmap vertical rhythm: consistent gap between nodes (~20px) so the spine
  reads as evenly spaced.

### 2.3 Grid & containers
- Chrome content max-width ~1100–1200px centered.
- Roadmap canvas is **narrower and centered** (~700–800px effective spine width)
  so the eye tracks one column; optional nodes branch into the side gutters.
- Home/directory: responsive card grid (1 / 2 / 3 cols).

### 2.4 Color palette (reconstructed)
- **Signature node yellow** `#FFE599`-ish fill / our adopted `#ffdf3d` for core,
  tan `#fbe5a6` for optional. **[ours, already shipped]**
- Hard black outline `#0F172A` + offset hard shadow → the "sticker/sketch" look.
- Progress states: green = done, yellow ring = in-progress, gray/struck = skipped.
- Chrome: near-white background, slate text, a single brand accent. Our brand
  gradient (`#009bda → #63c8cf → #e85aa7`) is *richer* than theirs — acceptable
  differentiation **[ours]**.

### 2.5 Borders, shadows, radius
- Nodes: `border-2` solid dark + **offset hard shadow** (`3px 3px 0 0`), small
  radius (~6px). This is the defining visual motif. **[already shipped in our detail page]**
- Chrome cards: soft shadow, larger radius (we use `rounded-3xl`).

### 2.6 Iconography & rhythm
- Minimal line icons in chrome; emoji/icon glyph per roadmap card.
- Visual rhythm in the roadmap = repetition of identical node shapes down a
  spine, broken by milestone notes and section labels — creating scannable
  "chapters".

**Reconciliation:** our shipped roadmap detail page already implements the
node/shadow/spine motif. The gap is that it is **static data + local progress
only**, with no graph engine, no resources drawer, no zoom/pan, no search.

---

## 3. Layout Analysis

### 3.1 Homepage
Hero + value prop → categorized grid of roadmap cards → secondary content
(guides/videos) → footer. Cards: icon, title, tiny meta, hover lift.

### 3.2 Roadmap page (the core template)
- Slim header (title, progress %, legend, actions).
- **Centered vertical flowchart**: spine + core nodes + branching optional nodes
  + section/milestone labels.
- Click node → **right drawer** with description + resources + progress buttons.
- Footer CTA (login to save progress, suggest changes, related roadmaps).

### 3.3 Learning / resource page
The drawer *is* the learning surface: title, 2–4 sentence explainer, then a list
of **typed resources** (article, video, official docs, course, feed, opensource)
each with a small type badge and a "mark resource done" affordance.

### 3.4 Sidebar / drawer behavior
- Right slide-over, ~420px, overlays canvas with scrim; ESC / click-out closes.
- URL updates to `/{slug}/{topic}` (deep-linkable, shareable, SEO).

### 3.5 Search layout
- Command-palette style (⌘K) global search across roadmaps, nodes, guides.
- Results grouped by type; keyboard navigable.

### 3.6 Card / section layouts
- Uniform card components; section dividers inside roadmaps act as chapter heads.

### 3.7 Responsive breakpoints **[ours, aligning to Tailwind]**
- `sm 640 / md 768 / lg 1024 / xl 1280`.
- Mobile roadmap: spine collapses to a single full-width column; optional nodes
  stack inline (no side-branching); drawer becomes a bottom sheet.

---

## 4. UX Analysis

### 4.1 Onboarding
Near-zero. You can use a roadmap with no account. Login is sold *after* value is
felt ("log in to save your progress"). **Adopt this** — matches our existing
localStorage-first roadmap.

### 4.2 Retention & motivation
- **Progress %** per roadmap + per-node states = visible, restartable goals.
- Returning users see their colored progress restored → sunk-cost momentum.
- Favorites / "my roadmaps" create a personal home.

### 4.3 Learning psychology
- **Chunking:** one node = one learnable concept.
- **Prerequisite clarity:** the spine removes "what do I learn next" anxiety.
- **Optional vs core** lowers guilt — you can skip branches guilt-free.
- **Just-in-time depth:** resources hidden until you ask (drawer), avoiding wall-of-links overwhelm.

### 4.4 Gamification (deliberately light)
Progress %, streak-ish completion, shareable progress. No heavy points/badges —
keeps it credible for professionals. **[We may add modest XP/badges since our
audience skews student — a divergence to decide.]**

### 4.5 Navigation / discovery / exploration
- Spine scroll = primary nav within a roadmap.
- Cross-links to related roadmaps + guides = lateral discovery.
- Search/⌘K = direct jump.

---

## 5. Interaction Analysis

| Interaction | Behavior to reproduce |
|---|---|
| Node hover | slight lift / shadow grow, cursor pointer if it has detail |
| Node click | open topic drawer + update URL; node gets focus ring |
| Progress toggle | cycle Done → In-progress → Skipped → none; optimistic UI |
| Drawer | slide-in 200–250ms ease-out; scrim fade; trap focus; ESC closes |
| Expand/collapse | section groups collapsible (large roadmaps) |
| Zoom/pan (desktop) | optional canvas zoom for very large graphs |
| Search ⌘K | open palette, debounced query, grouped results, arrow-key nav |
| Loading | skeleton spine + shimmer drawer; never a blank spinner |
| Filter | by category on directory; by resource type in drawer |
| Animations | respect `prefers-reduced-motion`; subtle, never blocking |

---

## 6. Technical Architecture Analysis (inferred + our mapping)

### 6.1 roadmap.sh (inferred)
- **Frontend:** Astro/React hybrid; roadmaps authored as a serialized editor
  format (node positions + ids) rendered to SVG/HTML; heavy static generation
  for SEO; client islands for interactivity (drawer, progress).
- **Backend:** Node/Express-ish API + MongoDB for user accounts, progress,
  favorites, AI features; content largely in-repo files (git-based CMS / PRs).
- **Auth:** email + OAuth; JWT/session.
- **Progress storage:** localStorage when anonymous, server-synced when logged in.
- **Search:** client index for roadmaps + server search for guides.
- **Rendering engine:** precomputed node geometry (x/y) authored in their editor;
  the renderer is largely a *layout player*, not a live auto-layout engine.

### 6.2 IT-Shaharcha mapping (what we actually build on)
| Concern | roadmap.sh (inferred) | IT-Shaharcha target |
|---|---|---|
| Frontend | Astro + React islands | **Next.js 16 learner app** (existing) |
| API edge | single Node API | **Spring Cloud Gateway** (existing, JWT) |
| Roadmap domain | Mongo + files | **learning-service** (Postgres) new tables |
| Auth | JWT/session | **JWT at gateway** (existing, HS384→RS256 path) |
| Progress (anon) | localStorage | localStorage (existing `roadmapProgress.ts`) |
| Progress (auth) | Mongo sync | new `roadmap_progress` table + sync endpoint |
| Search | client + server | Postgres FTS now → Elasticsearch later |
| AI | external LLM | new `ai` capability (path-gen, tutor, quiz) |
| Admin authoring | git PR / editor | **console app** drag-drop builder (new) |

**Key divergence (intentional):** they author node geometry by hand in a custom
editor. We will support **both** hand-positioned nodes *and* an auto-layout
fallback (deterministic spine layout from edges) so authors don't have to place
every pixel. This is a scalability win for a small team.

---

## 7. Roadmap Engine Analysis

### 7.1 Node structure (target model)
```
RoadmapNode {
  id (stable slug, unique within roadmap)
  roadmapId
  type: TOPIC | SUBTOPIC | SECTION_LABEL | MILESTONE | PARAGRAPH
  title, summary, detail (our original prose)
  optional: bool
  position: { x, y } | null   // null ⇒ auto-layout
  courseRef / lessonRef        // deep-link into our catalog
  order                        // tiebreaker for auto-layout
}
```

### 7.2 Connection system (edges)
```
RoadmapEdge {
  id, roadmapId
  fromNodeId, toNodeId
  kind: SEQUENCE (spine) | BRANCH (optional) | RELATED (cross-link)
  style: SOLID | DOTTED
}
```

### 7.3 Dependency & parent-child
- **SEQUENCE** edges define prerequisite order → topological sort yields the spine.
- **BRANCH** edges hang optional nodes off a parent (alternating gutters).
- A node's prerequisites = all nodes with a SEQUENCE edge into it. Used for
  "you should finish X before Y" hints and AI gap analysis.

### 7.4 Knowledge graph architecture
- Within a roadmap: a DAG (we validate no cycles on save).
- Across roadmaps: shared **Topic identity** table lets "Git" in Frontend and
  Backend point at one canonical topic + resource set (de-dup, single source of truth).

### 7.5 Dynamic rendering strategy
- **Server** returns roadmap graph JSON (nodes + edges + resources-count).
- **Client renderer**:
  1. If nodes have `position`, render at those coords (authored layout).
  2. Else run **deterministic auto-layout**: topological sort SEQUENCE edges into
     a vertical spine; assign BRANCH children to alternating left/right gutters
     (this is exactly what our current `roadmaps.ts` page approximates).
- Render as absolutely-positioned divs over an SVG edge layer (HTML nodes are
  accessible/SEO-friendlier than pure SVG text).

### 7.6 Expand/collapse logic
- SECTION_LABEL nodes can collapse their following run of nodes until the next
  section — for very large roadmaps. State persisted per-user (optional).

### 7.7 Resource attachment system
```
Resource {
  id, topicId (or nodeId)
  type: ARTICLE | VIDEO | OFFICIAL_DOCS | COURSE | FEED | OPENSOURCE | EXAM
  title, url | internalRef
  isPremium, isOfficial, addedBy, votes
}
```
For us, **internal-first**: resources prefer to deep-link to our own
courses/lessons/exams; external links are allowed but clearly typed.

---

## 8. Business Model Analysis

| Layer | roadmap.sh | IT-Shaharcha stance |
|---|---|---|
| Free core | all roadmaps + progress | **All free** (platform is free-education by charter) |
| Premium | AI features, some courses, teams | AI usage limits / org features *if ever monetized*; not now |
| Community | suggest-changes via GitHub PRs | console-based suggestions + teacher authoring |
| Moderation | maintainers review PRs | admin/teacher roles + review queue (existing RBAC) |
| Growth loops | SEO guides, shareable progress, embeds | SEO roadmap pages, shareable portfolio + progress, referral |
| Engagement | progress, AI tutor, new roadmaps | progress, AI tutor, exams, certificates, portfolio (our edge) |

**Our differentiator:** roadmap.sh points *out* to the web for learning. We point
*in* — every node can deep-link to our own course/lesson/exam, and completion
flows into the existing **portfolio + certificate** system. That closes the loop
roadmap.sh leaves open, and it's the strategic reason to build this rather than
just embed theirs.

---

## 9. Gap analysis — current IT-Shaharcha vs. target

| Capability | Today | Target |
|---|---|---|
| Roadmap data | static `roadmaps.ts` (4 roadmaps) | DB-backed graph, many roadmaps |
| Node geometry | auto spine in page code | authored + auto-layout engine |
| Edges | implicit (order/optional) | explicit typed edges (DAG) |
| Resources drawer | inline expand (summary/detail/1 course) | typed multi-resource drawer + URL deep-link |
| Progress | localStorage only | local + authenticated server sync |
| Search | none | ⌘K across roadmaps/nodes/guides |
| Admin builder | none (edit TS file) | drag-drop node/edge editor in console |
| AI | none | path-gen, gap analysis, tutor, quiz-gen |
| Dark mode | none | theme toggle |
| Cross-roadmap topics | none | shared topic identity |

This gap list becomes the backlog in `05-implementation-plan.md`.

---

## 10. Design principles we commit to (carried into Phase 2)

1. **Graph-as-data.** Roadmaps are rows, not React files. 3–4 templates render all.
2. **Stable node ids forever.** Progress and resources key off them; copy is mutable.
3. **Anonymous-first, sync-on-login.** Never gate the core experience.
4. **Internal-first resources.** Deep-link our catalog before the open web.
5. **Authored OR auto-layout.** Don't force pixel-placement to ship a roadmap.
6. **Original content only.** Visual language reproduced; curriculum prose ours.
7. **Accessible by construction.** HTML nodes, focus management, reduced-motion,
   keyboard ⌘K, ARIA on drawer.
