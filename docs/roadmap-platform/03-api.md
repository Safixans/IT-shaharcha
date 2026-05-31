# Phase 2 — API Design

> All routes are behind the API Gateway (`:8080`). Public reads are in the
> gateway `openPatterns`; everything that mutates or is user-scoped requires a
> Bearer access token (gateway injects `X-Account-Id` / `X-Roles`). Responses use
> the existing envelope `{ success, message, data, timestamp }`; lists use
> `Page<T> = { items, meta }`. Prefixes route to services:
> `/learning/**` → learning-service, `/ai/**` → ai-service.

---

## 1. Auth & visibility matrix

| Group | Auth | Roles |
|---|---|---|
| Catalog reads (`GET` roadmaps/nodes/search) | none (open) | — |
| Progress (`/me/...`) | required | any logged-in |
| Admin authoring | required | `ROLE_TEACHER` (author), `ROLE_ADMIN` (publish/moderate) |
| AI features | required | any logged-in (rate-limited) |

---

## 2. Public roadmap catalog (open)

```
GET  /learning/roadmaps
       ?category=&kind=&difficulty=&q=&page=&size=
       → Page<RoadmapCard>            // directory grid
GET  /learning/roadmaps/categories
       → Category[]                    // for filters/sections
GET  /learning/roadmaps/{slug}
       → RoadmapDetail                 // meta + nodes + edges (the graph)
GET  /learning/roadmaps/{slug}/nodes/{nodeKey}
       → NodeDetail                    // drawer payload: detail + resources
GET  /learning/topics/{topicSlug}
       → TopicDetail                   // shared topic + resources
```

`RoadmapDetail` (the render payload):
```jsonc
{
  "id": "uuid", "slug": "frontend", "title": "...", "tagline": "...",
  "icon": "🎨", "kind": "role", "difficulty": "beginner",
  "layoutMode": "auto",                 // auto ⇒ client computes geometry
  "nodes": [
    { "id":"uuid","nodeKey":"html","type":"topic","title":"HTML",
      "summary":"...","optional":false,"posX":null,"posY":null,
      "orderIndex":1,"courseId":"uuid|null","resourceCount":4 }
  ],
  "edges": [
    { "fromNodeKey":"html","toNodeKey":"css","kind":"sequence","style":"solid" }
  ]
}
```
> Edges reference `nodeKey` (stable) not row ids, so the payload is cache-friendly
> and stable across re-saves.

`NodeDetail` (drawer):
```jsonc
{
  "nodeKey":"css","title":"CSS","summary":"...","detail":"<original prose>",
  "optional":false,
  "courseRef": { "id":"uuid","title":"CSS Fundamentals","slug":"css" },
  "resources":[
    {"id":"uuid","type":"course","title":"CSS Fundamentals","internalRef":"uuid","isOfficial":true},
    {"id":"uuid","type":"article","title":"...","url":"https://...","isPremium":false}
  ]
}
```

---

## 3. Progress (authenticated, `/learning/me/...`)

```
GET   /learning/me/roadmaps/{slug}/progress
        → { nodeKey: "done"|"in_progress"|"skipped", ... }
PUT   /learning/me/roadmaps/{slug}/progress/{nodeKey}
        body { state: "done"|"in_progress"|"skipped" }
        → updated summary { percent, doneCount, totalCount }
DELETE /learning/me/roadmaps/{slug}/progress/{nodeKey}     // un-toggle → NONE
        → updated summary
POST  /learning/me/roadmaps/{slug}/progress/merge
        body { "html":"done","css":"in_progress" }          // local → server
        → canonical { nodeKey: state }                       // server-wins by updated_at
GET   /learning/me/dashboard
        → { roadmaps:[{slug,title,percent,lastActiveAt}], streakDays, recentNodes }
```
Mutations are **optimistic** on the client; failures roll back. A successful
authenticated write publishes `roadmap.progress.updated` to Kafka.

---

## 4. Search

```
GET /learning/search?q=react&types=roadmap,node,course&limit=20
    → {
        roadmaps:[{slug,title,icon}],
        nodes:[{roadmapSlug,nodeKey,title,roadmapTitle}],
        courses:[{slug,title}]
      }
```
- Open endpoint (anonymous ⌘K works).
- Phase A: Postgres FTS. Phase B: same contract, served by search-service.

---

## 5. Admin authoring (`/learning/admin/roadmaps/...`, ROLE_TEACHER/ADMIN)

```
POST   /learning/admin/roadmaps                       // create draft
         body { slug,title,tagline,kind,difficulty,layoutMode }
PATCH  /learning/admin/roadmaps/{id}                  // meta edits (autosave)
DELETE /learning/admin/roadmaps/{id}                  // soft delete

POST   /learning/admin/roadmaps/{id}/nodes            // add node
PATCH  /learning/admin/nodes/{nodeId}                 // title/summary/detail/optional/course/pos
DELETE /learning/admin/nodes/{nodeId}
PATCH  /learning/admin/roadmaps/{id}/nodes/positions  // bulk drag save [{nodeId,x,y}]

POST   /learning/admin/roadmaps/{id}/edges            // {fromNodeId,toNodeId,kind,style}
DELETE /learning/admin/edges/{edgeId}

POST   /learning/admin/topics                         // shared topic
POST   /learning/admin/topics/{id}/resources          // attach typed resource
PATCH  /learning/admin/resources/{id}
DELETE /learning/admin/resources/{id}
POST   /learning/admin/nodes/{nodeId}/resources       // link existing resource to node

POST   /learning/admin/roadmaps/{id}/publish          // validate DAG → snapshot version → status=published
POST   /learning/admin/roadmaps/{id}/rollback/{versionNo}
GET    /learning/admin/roadmaps/{id}/versions
```
- `publish` runs server-side **cycle validation**; returns `422` with the offending
  edge list if the graph is not acyclic.
- All admin writes set `updated_by` from `X-Account-Id` and bump `version`
  (optimistic locking → `409` on stale write).

### Moderation
```
GET   /learning/admin/suggestions?status=pending&page=&size=
POST  /learning/admin/suggestions/{id}/approve        // applies payload, audits
POST  /learning/admin/suggestions/{id}/reject  { reason }
```

### Community suggestions (authenticated, any role)
```
POST  /learning/roadmaps/{slug}/suggestions
        body { kind:"add_resource"|"edit_text"|"report"|"new_node", nodeKey?, payload }
        → { id, status:"pending" }
```

---

## 6. AI service (`/ai/...`, authenticated, rate-limited)

```
POST /ai/paths/generate
       body { goal:"become a frontend dev", level:"beginner", hoursPerWeek:8,
              knownTopics:["html"] }
       → { draftRoadmap: RoadmapDetail-shape, rationale, suggestedResources }
       // returned as a DRAFT; user can save → creates a personal/draft roadmap

POST /ai/gap-analysis
       body { roadmapSlug, progress:{nodeKey:state} }
       → { nextTopics:[{nodeKey,title,why,priority}], coveragePercent }

POST /ai/tutor/chat
       body { roadmapSlug?, nodeKey?, message, history?[] }
       → { reply, citations:[{type,title,ref}] }   // RAG-grounded in our content

POST /ai/quiz/generate
       body { nodeKey|courseId, count:5, difficulty:"medium" }
       → { questions:[{prompt,choices,answerIndex,explanation}] }
       // can be pushed to assessment-service to persist as a quiz

POST /ai/roadmaps/draft           // ROLE_TEACHER/ADMIN
       body { topic:"Rust backend", depth:"standard" }
       → { draftGraph } // editor refines before publish
```

Cross-cutting AI rules:
- **Strict JSON schemas** validated server-side; malformed model output → `502`
  with retry, never persisted.
- **Grounding:** tutor/quiz retrieve from learning-service (Feign) and must cite
  internal content where possible.
- **Rate limit:** Redis token bucket per `account_id`; `429` with `Retry-After`.
- **Never auto-publishes** to catalog — output is draft/suggestion only.

---

## 7. Error model (inherited)

RFC-7807-style via `common-lib` `@RestControllerAdvice`:
```jsonc
{ "success": false,
  "message": "Roadmap graph contains a cycle",
  "error": { "code":"ROADMAP_CYCLE", "details":[{"from":"a","to":"b"}] },
  "timestamp": "2026-05-31T..." }
```
| Code | HTTP | When |
|---|---|---|
| `ROADMAP_NOT_FOUND` | 404 | unknown slug |
| `NODE_NOT_FOUND` | 404 | unknown nodeKey |
| `ROADMAP_CYCLE` | 422 | publish/save with cycle |
| `EDGE_ENDPOINT_INVALID` | 422 | edge to node in another roadmap |
| `STALE_VERSION` | 409 | optimistic lock conflict |
| `AI_RATE_LIMITED` | 429 | bucket exhausted |
| `AI_UPSTREAM` | 502 | LLM error / invalid output |

---

## 8. `@itsh/api-client` additions (TypeScript surface)

```ts
// reads (auth defaults true in request<T>(); pass auth:false for open GETs that
// must also work anonymously — but progress/admin/ai stay authed)
listRoadmaps(q?): Promise<Page<RoadmapCard>>
getRoadmap(slug): Promise<RoadmapDetail>
getNode(slug, nodeKey): Promise<NodeDetail>
searchAll(q, types?): Promise<SearchResults>

// progress
getProgress(slug): Promise<Record<string, NodeState>>
setNodeState(slug, nodeKey, state): Promise<ProgressSummary>
clearNodeState(slug, nodeKey): Promise<ProgressSummary>
mergeProgress(slug, local): Promise<Record<string, NodeState>>
getDashboard(): Promise<Dashboard>

// admin
createRoadmap(input); patchRoadmap(id, input); publishRoadmap(id)
addNode(id, input); patchNode(nodeId, input); saveNodePositions(id, positions)
addEdge(id, input); deleteEdge(edgeId)
addTopicResource(topicId, input)

// ai
aiGeneratePath(input); aiGapAnalysis(input); aiTutorChat(input); aiQuizGenerate(input)
```
Types (`RoadmapCard`, `RoadmapDetail`, `NodeDetail`, `NodeState`,
`ProgressSummary`, `SearchResults`, `Dashboard`) are exported from
`@itsh/api-client` so all three apps share them.
