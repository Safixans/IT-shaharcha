# IT-Shaharcha — API Contracts

Contract-first OpenAPI 3.1 specs. These files are the **source of truth** for the
wire contract between the frontend and the backend. Change the spec first; code
follows the spec, not the other way around.

## Why this exists

The platform is split into a small number of broad services (not 100 micro-apps).
Every service speaks an identical envelope, errors, pagination, and security
scheme, and every service emits events + exposes the same analytics read API. That
uniformity is the whole point: the frontend learns one set of conventions and
reuses them everywhere; a new service is mostly "fill in the domain paths".

## Services (5)

| Service | Base path | Covers |
|---|---|---|
| `identity` | `/api/v1` (`/auth`, `/identity`) | User/auth, profile, accounts, roles |
| `learning` | `/api/v1/learning` | Learn IT skills, watch tutorials, read docs, practice typing |
| `assessment` | `/api/v1/assessment` | Prepare for IELTS/SAT, take real mock exams, scoring |
| `portfolio` | `/api/v1/portfolio` | Upload certificates, build the academic portfolio |
| `analytics` | `/api/v1/analytics` | Track progress, participate in rankings, view analytics |

Each lives in `<service>.openapi.yaml` at the root of this folder.

## Layout

```
specs/
  shared/
    common.yaml         # envelope, errors, pagination, security, common params + responses
    events.yaml         # canonical DomainEvent + EventType catalog + payload schemas
    analytics-api.yaml  # the identical analytics read sub-API (path items + schemas)
  identity.openapi.yaml
  learning.openapi.yaml
  assessment.openapi.yaml
  portfolio.openapi.yaml
  analytics.openapi.yaml
  README.md
```

Service specs `$ref` the three `shared/*` files with relative paths so the contract
is defined once and reused. OpenAPI 3.1 has no generics, so typed bodies use:

```yaml
allOf:
  - $ref: "./shared/common.yaml#/components/schemas/ApiResponse"
  - type: object
    properties:
      data: { $ref: "#/components/schemas/MyType" }
```

## Platform conventions

- **Versioning** — everything under `/api/v1`. The gateway routes by the path
  prefix to the owning service.
- **Success envelope** — `ApiResponse` `{ success: true, message?, data, timestamp }`.
- **Error envelope** — `ErrorResponse` `{ success: false, error: { code, message,
  fieldErrors? }, timestamp, path?, traceId? }`. Clients branch on the stable
  `ErrorCode` enum, never on message text.
- **Pagination** — `page` (0-based), `size` (≤100), repeatable `sort=field,asc|desc`;
  responses use the `Page` `{ items, meta }` envelope.
- **Auth** — `bearerAuth` (JWT) issued by `identity`. The gateway validates the
  token and forwards `X-Account-Id` / `X-Username` / `X-Roles` to downstream
  services; clients never send those headers. Public endpoints set `security: []`.
- **Action endpoints** — non-CRUD verbs use the `:verb` suffix (e.g.
  `/courses/{id}:enroll`, `/sessions/{id}:submit`, `/events:ingest`).
- **Idempotency** — unsafe POSTs accept an optional `Idempotency-Key` header.
- **IDs** — UUIDs. **Money** — minor units (`amount` + `currency`).

## Analytics contract (the uniform part)

Two halves, both defined once and reused:

1. **Events (write side)** — every user-meaningful action produces a `DomainEvent`
   (`shared/events.yaml`) onto Kafka topic `itsh.<service>.events`, keyed by
   `actor.accountId` so a user's events stay ordered. `EventType` is a closed,
   versioned catalog named `<service>.<entity>.<pastTenseVerb>`. The `analytics`
   service is the primary consumer; a REST fallback at
   `POST /api/v1/analytics/events:ingest` accepts the same envelope for
   broker-less environments and backfills.

2. **Read API (read side)** — every service mounts the identical three paths from
   `shared/analytics-api.yaml` under its own base path:
   - `…/analytics/summary` — compact per-domain roll-up (`DomainAnalyticsSummary`)
   - `…/analytics/activity` — paginated feed of that domain's `DomainEvent`s
   - `…/analytics/metrics` — time-bucketed `MetricSeries`

   Each service answers from its own domain. The `analytics` service answers the
   same shapes from the aggregated cross-domain store, and adds the cross-cutting
   paths: `/progress`, `/milestones`, `/rankings`, `/rankings/me`, `/dashboard`.

So a client can call analytics on any service uniformly, and the consumer powering
"Track personal progress" / "View analytics" / rankings is fed by the event stream.

## Admin surface (the admin panel)

Authoring/management endpoints live under an `/admin/` segment within each owning
service and are tagged `admin`. They require an elevated role — `ROLE_ADMIN`, or
`ROLE_TEACHER` for content authoring — enforced by the service (others get `403`).
This is the contract the admin panel UI is built against.

- **Learning content** (`/learning/admin/…`) — CRUD for `tracks`, `courses`,
  `modules`, `lessons`, `tutorials`, `docs`, `typing/lessons`.
- **External feeds** (`/learning/admin/sources`) — register a `ContentSource`
  (`rss` / `youtube` / `sitemap` / `api`) that syncs tutorials or docs in; CRUD
  plus `…/sources/{id}:sync` to trigger an immediate pull. Ingested items carry
  the owning `sourceId`.
- **Exam authoring** (`/assessment/admin/…`) — CRUD for `exams`, their `sections`,
  and `questions` (with `choices` + `correctAnswer`).
- **Accounts & roles** (`/identity/…`) — `accounts/{id}:suspend` and
  `:activate`, `PUT accounts/{id}/roles` to set a user's roles, and a role catalog
  (`POST /roles`, `DELETE /roles/{roleName}`).

Authoring writes flow through the same event contract — creating/updating content
or accounts emits the relevant `DomainEvent`, so analytics stays consistent.

Create/update bodies use dedicated `*Input` schemas (e.g. `CourseInput`,
`ExamInput`, `ContentSourceInput`); `PATCH` treats all fields as optional while
`POST` enforces the schema's `required` set. Repeated typed success bodies are
factored into `components.responses` (e.g. `CourseOk`, `ExamOk`) to keep paths DRY.

## Working with the specs

These tools are not vendored; install on demand. Validate/lint, then optionally
bundle a service (resolving `$ref`s) into a single file for codegen or docs:

```bash
# Lint (Redocly CLI)
npx @redocly/cli lint specs/identity.openapi.yaml

# Bundle into one self-contained file
npx @redocly/cli bundle specs/learning.openapi.yaml -o build/learning.bundled.yaml

# Preview docs
npx @redocly/cli preview-docs specs/assessment.openapi.yaml

# Alternatively, validate with Swagger CLI
npx @apidevtools/swagger-cli validate specs/portfolio.openapi.yaml
```

The `shared/*` files are component libraries (`paths: {}`) — lint them only via a
service spec that references them, not standalone.

### Viewing in Swagger UI — load the bundled file, not the raw source

The raw service specs use cross-file `$ref`s (`./shared/common.yaml#/...`). A plain
Swagger UI / raw viewer cannot resolve those, so shared types render as empty
(`data: {}`) or as a bare `"string"` placeholder (e.g. the `401` error body). The
**bundled** output inlines every `$ref` into one self-contained file that renders
the full request/response DTOs and examples correctly. Always point Swagger UI at
`build/<service>.bundled.yaml`, and re-bundle after editing a spec:

```bash
# Re-bundle all five services into specs/build/
for s in identity learning assessment portfolio analytics; do
  npx @redocly/cli@latest bundle specs/$s.openapi.yaml -o specs/build/$s.bundled.yaml
done
```

`specs/build/` is generated — treat the `*.openapi.yaml` sources as the source of
truth and the bundles as a build artifact.

## Evolution rules

- Add optional fields only; never repurpose or remove an existing field.
- New event types are additive entries in the `EventType` enum.
- `specVersion` (const `1.0`) pins the envelope shape; bump it only for a breaking
  envelope change, which would be a new major path version (`/api/v2`).
