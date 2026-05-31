# Phase 2 — Component & Folder Structure (Frontend)

> Targets the existing Next.js monorepo: `frontend/apps/{learner,console,public}`
> + shared `frontend/packages/{api-client,auth}`. New shared package
> `@itsh/roadmap` holds the layout engine + rendering primitives reused by
> learner (view) and console (build).

---

## 1. New shared package: `@itsh/roadmap`

```
frontend/packages/roadmap/
├── package.json
├── src/
│   ├── layout/
│   │   ├── autoLayout.ts        // edges → {nodeKey:{x,y}} deterministic spine
│   │   ├── topoSort.ts          // Kahn's algo over sequence edges (+cycle detect)
│   │   └── geometry.ts          // bounding box, gutter assignment, edge paths
│   ├── render/
│   │   ├── RoadmapCanvas.tsx     // positions nodes over EdgeLayer; auto|manual
│   │   ├── EdgeLayer.tsx         // single SVG, one <path> per edge (solid/dotted)
│   │   ├── RoadmapNode.tsx       // the sticker box (core yellow / optional tan)
│   │   ├── SectionLabel.tsx
│   │   └── MilestoneNote.tsx
│   ├── types.ts                  // RoadmapGraph, Node, Edge, NodeState (mirror api-client)
│   └── index.ts
```

Why a package: the **canvas + node primitives are identical** between viewing
(learner) and building (console). The only difference is interaction handlers
(click-to-open-drawer vs. drag/select), injected as props. Layout is pure and
unit-testable in isolation (snapshot the geometry for a given graph).

### Key contracts
```ts
// autoLayout is pure → same graph yields same coordinates (SSR-safe, testable)
function autoLayout(graph: RoadmapGraph): Map<nodeKey, {x:number;y:number}>

// RoadmapCanvas renders authored positions if layoutMode==='manual', else autoLayout
<RoadmapCanvas
  graph={graph}
  progress={progressMap}
  selectedKey={openTopic}
  onNodeClick={(key) => ...}        // viewer: open drawer
  editor={{ onDragNode, onConnect }} // console: enables drag + edge-draw
/>
```

---

## 2. Learner app — roadmap experience

```
frontend/apps/learner/app/(app)/
├── roadmap/
│   ├── page.tsx                  // directory: categories + RoadmapCard grid + ⌘K
│   └── [slug]/
│       ├── page.tsx              // RoadmapCanvas + TopicDrawer (reads ?topic=)
│       └── loading.tsx           // skeleton spine + shimmer
├── dashboard/page.tsx            // "my roadmaps" progress widgets
└── ...
frontend/apps/learner/components/roadmap/
├── RoadmapHeader.tsx             // title, progress %, legend, actions
├── ProgressLegend.tsx
├── TopicDrawer.tsx               // right slide-over; focus-trap; ESC; ?topic= URL
├── ResourceList.tsx              // typed resource rows + type badge
├── ResourceItem.tsx              // internal deep-link vs external (rel=noopener)
├── ProgressButtons.tsx           // Done / In-progress / Skipped cycle (optimistic)
├── RoadmapCard.tsx               // directory card
├── CommandPalette.tsx            // ⌘K global search overlay
└── ThemeToggle.tsx
frontend/apps/learner/lib/
├── roadmapProgress.ts            // EXTEND: keep local API, add server-sync + merge
├── useRoadmap.ts                 // TanStack Query hooks: useRoadmap, useNode, useProgress
└── useTheme.ts
```

### State & data flow (learner)
- **TanStack Query** (new dep) caches `getRoadmap`, `getNode`, `getProgress`.
- **Anonymous progress:** `roadmapProgress.ts` localStorage (existing) is the
  write-through cache; if logged in, `setNodeState` also fires the API
  (optimistic). On login, `mergeProgress` reconciles.
- **Drawer open state lives in the URL** (`?topic=css`) → shareable + back-button
  friendly; `TopicDrawer` reads `useSearchParams`.
- **Theme:** `ThemeToggle` sets `class="dark"` on `<html>`, persists to
  localStorage, defaults to system preference; Tailwind `darkMode:'class'`.

### Accessibility specifics
- Nodes are `<button>` with `aria-expanded`/`aria-pressed`; visible focus ring.
- Drawer: `role="dialog" aria-modal`, focus trap, returns focus to node on close.
- ⌘K palette: `role="combobox"` + `listbox`, arrow-key navigation, `aria-activedescendant`.
- All transitions gated by `prefers-reduced-motion`.

---

## 3. Console app — roadmap builder

```
frontend/apps/console/app/(panel)/roadmaps/
├── page.tsx                      // list of roadmaps (draft/published) + "New"
└── [id]/
    ├── page.tsx                  // builder shell
    └── ...
frontend/apps/console/components/roadmap-builder/
├── BuilderCanvas.tsx             // wraps @itsh/roadmap RoadmapCanvas with editor props
├── NodePalette.tsx               // drag-to-add node types
├── NodeInspector.tsx             // right panel: edit title/summary/detail/optional/course
├── EdgeTool.tsx                  // click from→to to connect; pick kind/style
├── ResourceManager.tsx           // attach typed resources to a node/topic
├── PublishBar.tsx                // Save state, Validate, Publish, Versions, Rollback
├── ValidationBanner.tsx          // shows cycle/endpoint errors from 422
└── SuggestionQueue.tsx           // moderation: approve/reject community edits
```

### Builder data flow
- **Autosave:** debounced `PATCH` on node/meta edits; drag emits
  `saveNodePositions` (bulk) on drop.
- **Edge draw:** `EdgeTool` selects source node, then target → `POST edges`.
- **Publish:** `PublishBar` → `publishRoadmap`; on `422 ROADMAP_CYCLE` renders
  `ValidationBanner` highlighting offending edges on the canvas.
- **Optimistic-lock aware:** `409 STALE_VERSION` → prompt reload (no silent overwrite).

---

## 4. Public app (SEO surface)

```
frontend/apps/public/app/
├── roadmaps/page.tsx             // SSR directory (indexable)
└── roadmap/[slug]/page.tsx       // SSR/ISR roadmap (read-only, login CTA to track)
```
- Server-rendered for SEO; uses `api.serverListTracks`-style server fetch (existing
  pattern) with graceful empty fallback.
- Includes per-roadmap `<title>`/OG metadata + JSON-LD `Course`/`ItemList`.
- Read-only: progress controls show "Log in to track" → routes to learner app.

---

## 5. Design-system reuse

No new visual language — reuse shipped tokens:
- `bg-brand-gradient`, `shadow-lift/soft/card`, `rounded-3xl`, `.btn-*`,
  `.card-hover`, `.section-eyebrow`, `text-gradient` (in `globals.css`).
- Node sticker style already exists in `roadmap/[slug]/page.tsx`
  (`border-2 border-slate-900 shadow-[3px_3px_0_0_...]`, `#ffdf3d`/`#fbe5a6`) →
  lift it into `@itsh/roadmap`'s `RoadmapNode` unchanged.
- **Dark mode addition:** define `dark:` variants for surfaces (slate-900 bg,
  slate-100 text) and keep node yellow/tan (they read on dark too); add
  `darkMode:'class'` to all three `tailwind.config.ts`.

---

## 6. Component dependency graph (who renders what)

```
learner roadmap/[slug]/page.tsx
  ├─ RoadmapHeader (ProgressLegend)
  ├─ @itsh/roadmap RoadmapCanvas
  │     ├─ EdgeLayer (SVG)
  │     └─ RoadmapNode × N  (+ SectionLabel, MilestoneNote)
  ├─ TopicDrawer  (reads ?topic=)
  │     ├─ ResourceList → ResourceItem × N
  │     └─ ProgressButtons (optimistic via useProgress)
  └─ CommandPalette (⌘K, useSearch)

console roadmaps/[id]/page.tsx
  ├─ BuilderCanvas → @itsh/roadmap RoadmapCanvas (editor props)
  ├─ NodePalette / EdgeTool
  ├─ NodeInspector / ResourceManager
  └─ PublishBar (+ ValidationBanner, versions)
```

---

## 7. New dependencies (minimal, justified)

| Dep | Where | Why |
|---|---|---|
| `@tanstack/react-query` | learner, console | server-cache, dedupe, optimistic progress |
| (none for layout) | `@itsh/roadmap` | layout written in-house (deterministic, testable) |
| (drag) native pointer events or `@dnd-kit` | console only | builder drag/drop; keep out of learner bundle |

`@itsh/roadmap` ships **no heavy graph lib** — auto-layout is a small topological
sort + gutter assignment, matching the spine aesthetic and keeping the learner
bundle lean.
