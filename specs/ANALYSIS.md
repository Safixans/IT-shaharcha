# IT-Shaharcha — Repository Analysis

> Written 2026-05-29. A snapshot of what the repo currently contains, how the
> pieces fit together, and where the code diverges from the contracts in this
> `specs/` folder. The OpenAPI specs here are the source of truth; this doc
> explains the gap between that target and the code on disk today.

## 1. What the platform is

A free-education EdTech platform: IT learning, IELTS/SAT prep + exam simulation,
typing practice, analytics/leaderboards, and student portfolios. Backend is
Java 21 / Spring Boot 3.4 / Spring Cloud 2024 microservices behind an API
gateway, with PostgreSQL (DB-per-service), Redis, Kafka, and Eureka discovery.
Frontend is Next.js + TypeScript + Tailwind, talking only to the gateway.

## 2. The two competing designs in this repo

There are **two generations of architecture** layered on top of each other, and
this is the single most important thing to understand:

- **Old design** — `docs/ARCHITECTURE.md`. ~14 fine-grained microservices:
  `auth`, `user`, `ielts`, `sat`, `it-learning`, `video`, `typing`, `analytics`,
  `leaderboard`, `notification`, `file-storage`, `admin`, plus `config-server`
  and `discovery`. This is the original plan and it is **superseded**.

- **New design** — this `specs/` folder (`specs/README.md`). Collapses everything
  into **5 broad services** that share one envelope, error model, pagination,
  security scheme, event contract, and analytics read API.

The code on disk is mid-migration: it still contains old-design services
(`auth-service`, `user-service`) alongside their new-design replacement
(`identity-service`).

## 3. Target architecture — the 5 services (source of truth)

| Service | Base path | Covers | Spec file |
|---|---|---|---|
| `identity` | `/api/v1` (`/auth`, `/identity`) | accounts, auth/JWT, OTP, profiles, roles | `identity.openapi.yaml` |
| `learning` | `/api/v1/learning` | tracks/courses/modules/lessons, tutorials, docs, typing | `learning.openapi.yaml` |
| `assessment` | `/api/v1/assessment` | IELTS/SAT prep, mock exams, scoring | `assessment.openapi.yaml` |
| `portfolio` | `/api/v1/portfolio` | certificates, academic portfolio | `portfolio.openapi.yaml` |
| `analytics` | `/api/v1/analytics` | progress, rankings, dashboards, cross-domain analytics | `analytics.openapi.yaml` |

Plus non-domain infrastructure (kept regardless of the spec list): **eureka**
(discovery), **api-gateway** (edge), **common-lib** (shared library), and the
**frontend**.

### Shared contract (`specs/shared/`)
- `common.yaml` — `ApiResponse` success envelope, `ErrorResponse` + `ErrorCode`
  enum, `Page`/pagination, `bearerAuth`, common params/responses.
- `events.yaml` — canonical `DomainEvent` + closed `EventType` catalog
  (`<service>.<entity>.<pastTenseVerb>`) + payload schemas.
- `analytics-api.yaml` — the identical 3-path analytics read sub-API
  (`/summary`, `/activity`, `/metrics`) every service mounts under its base path.

### Platform conventions (from `specs/README.md`)
- Everything under `/api/v1`; gateway routes by path prefix.
- Auth: JWT `bearerAuth` minted by `identity`; gateway validates and forwards
  `X-Account-Id` / `X-Username` / `X-Roles`; clients never send those.
- Non-CRUD verbs use `:verb` suffix (`/courses/{id}:enroll`, `/events:ingest`).
- Events: every meaningful action emits a `DomainEvent` to Kafka topic
  `itsh.<service>.events` keyed by `actor.accountId`; `analytics` is primary
  consumer, with a REST fallback `POST /api/v1/analytics/events:ingest`.
- Admin/authoring endpoints live under an `/admin/` segment, tagged `admin`,
  gated by `ROLE_ADMIN` / `ROLE_TEACHER`.
- IDs are UUIDs; money in minor units; `specVersion` const `1.0`.
- `specs/build/*.bundled.yaml` are generated artifacts (re-bundle with Redocly);
  raw `*.openapi.yaml` are the source of truth.

## 4. What actually exists on disk (`services/`)

| Directory | Generation | Maps to spec? | Java files | Verdict |
|---|---|---|---|---|
| `common-lib` | shared | infra (libs) | — | **keep** |
| `eureka-server` | shared | infra (eureka) | 1 | **keep** |
| `api-gateway` | shared | infra (gateway) | 5 | **keep** |
| `identity-service` | **new** | `identity` ✓ | 67 | **keep** |
| `learning-service` | **new** | `learning` ✓ | 104 | **keep** |
| `auth-service` | old | — (replaced by `identity`) | 39 | **delete** |
| `user-service` | old | — (replaced by `identity` + future `portfolio`) | 24 | **delete** |
| `frontend` | — | UI | — | **keep** |

Not yet implemented (spec exists, no service): **`assessment`**, **`portfolio`**,
**`analytics`**.

### Why auth-service and user-service are obsolete
`identity-service` already absorbs both:
- From `auth-service`: `AuthController` (register/login/refresh/verify-otp), JWT
  provider, accounts, roles, refresh tokens, OTP, `user.registered` events.
- From `user-service`: `ProfileController`, `Profile` entity, profile read/update.

`identity-service` adds `AccountController`, `RoleController`, and the uniform
`AnalyticsController` the old services never had. The `portfolio` half of
`user-service` (certificates, education, portfolio items) is slated for the
future `portfolio` service, not retained in identity.

## 5. How it wires together today

- **Parent `pom.xml`** aggregates 7 modules: common-lib, eureka-server,
  auth-service, identity-service, learning-service, api-gateway, user-service.
- **`infra/docker/docker-compose.full.yml`** runs: postgres, redis,
  eureka-server, auth-service (:9001), identity-service (:9003),
  learning-service (:9004), user-service (:9002), api-gateway (:8080).
- **`api-gateway` routes** (`application.yml`): `identity-service`
  (`/api/v1/auth/**`, `/api/v1/identity/**`), `learning-service`
  (`/api/v1/learning/**`), and a legacy `user-service` route
  (`/api/v1/users/**`, `/api/v1/profiles/**`).
- **`infra/docker/init-db.sql`** still creates the full old-design DB set
  (auth_db, users_db, ielts_db, sat_db, video_db, typing_db, leaderboard_db,
  notification_db, files_db, …) — most for services that no longer exist in the
  new design.
- **Frontend** (`next.config.mjs`) proxies `/api/*` → gateway `:8080`.

## 6. Cleanup implied by the spec model

To make the repo reflect the 5-service design (keeping eureka, gateway, frontend,
libs), the obsolete old-generation services and their wiring should go:

1. Delete `services/auth-service` and `services/user-service`.
2. Remove both `<module>` entries from `pom.xml`.
3. Remove the `auth-service` + `user-service` blocks from
   `docker-compose.full.yml` (and the `user-service` `depends_on` /
   `USER_SERVICE_URI` on the gateway).
4. Remove the legacy `user-service` route from `api-gateway/application.yml`.
5. Trim `init-db.sql` to the surviving/planned spec databases
   (identity, learning, assessment, portfolio, analytics).
6. Update `README.md` (its layout/run sections still describe the old
   auth+user+gateway setup).

`docs/ARCHITECTURE.md` describes the superseded 14-service design and conflicts
with `specs/`; it should eventually be reconciled to the 5-service model, but
that is a doc rewrite rather than a deletion.
