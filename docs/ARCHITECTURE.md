# IT-Shaharcha — Enterprise Educational Platform

A free-education EdTech ecosystem: IT learning, IELTS/SAT preparation, real exam
simulation, typing practice, analytics, leaderboards, and student portfolios.

This document is the source of truth for the system design. Code is built
module-by-module; see the [Build Roadmap](#build-roadmap) for current status.

---

## 1. System Overview

```
                              ┌───────────────────┐
                              │      Clients      │
                              │  Web (Next.js)    │
                              │  Mobile (future)  │
                              └─────────┬─────────┘
                                        │ HTTPS
                              ┌─────────▼─────────┐
                              │    API Gateway    │  Spring Cloud Gateway
                              │  routing, JWT,    │  rate-limit, CORS
                              │  rate limiting    │
                              └─────────┬─────────┘
                                        │
        ┌───────────────┬───────────────┼───────────────┬───────────────┐
        │               │               │               │               │
   ┌────▼────┐    ┌─────▼────┐    ┌─────▼─────┐   ┌──────▼─────┐   ┌─────▼─────┐
   │  Auth   │    │   User   │    │  IELTS    │   │   SAT      │   │   IT      │
   │ Service │    │ Service  │    │  Service  │   │  Service   │   │ Learning  │
   └────┬────┘    └─────┬────┘    └─────┬─────┘   └──────┬─────┘   └─────┬─────┘
        │               │               │                │               │
   ┌────▼────┐    ┌─────▼────┐    ┌─────▼─────┐   ┌──────▼─────┐   ┌─────▼─────┐
   │  Video  │    │  Typing  │    │ Analytics │   │Leaderboard │   │  Notif.   │
   │ Service │    │ Service  │    │  Service  │   │  Service   │   │  Service  │
   └─────────┘    └──────────┘    └───────────┘   └────────────┘   └───────────┘
        │               │               │                │               │
   ┌────▼────┐    ┌─────▼─────────────────────────────────▼───────────────▼───┐
   │  File   │    │           Cross-cutting infrastructure                     │
   │ Storage │    │  Config Server · Service Discovery (Eureka) · Admin Svc    │
   └─────────┘    └────────────────────────────────────────────────────────────┘

  Shared infra:  PostgreSQL (per-service DB) · Redis · Kafka · Elasticsearch
```

### Design principles
- **Database-per-service.** Each microservice owns its schema. No cross-service
  SQL joins; data is shared via REST/Feign or Kafka events.
- **Stateless services.** JWTs carry identity; no server-side session. Horizontally
  scalable behind the gateway.
- **Async where it pays.** Score-completed, exam-finished, certificate-uploaded, etc.
  are published to Kafka; Analytics/Leaderboard/Notification consume them.
- **Clean architecture per service.** controller → service (interface) → serviceImpl
  → repository → entity, with dto/mapper boundaries. No leaking entities over HTTP.

---

## 2. Microservice Breakdown

| Service          | Port  | DB schema          | Owns                                            | Key events (produces → consumes) |
|------------------|-------|--------------------|-------------------------------------------------|----------------------------------|
| api-gateway      | 8080  | —                  | routing, edge JWT check, rate limiting          | — |
| config-server    | 8888  | —                  | centralized config                              | — |
| discovery-server | 8761  | —                  | Eureka registry                                 | — |
| auth-service     | 9001  | `auth`             | accounts, credentials, roles, tokens, OTP       | produces `user.registered` |
| user-service     | 9002  | `users`            | profiles, portfolio, certificates, education    | consumes `user.registered`; produces `certificate.uploaded` |
| ielts-service    | 9003  | `ielts`            | listening/reading/writing/speaking, scoring     | produces `exam.completed` |
| sat-service      | 9004  | `sat`              | math/english, timed mocks, scoring              | produces `exam.completed` |
| it-learning      | 9005  | `learning`         | courses, modules, lessons, learning paths       | produces `lesson.completed` |
| video-service    | 9006  | `video`            | YouTube playlists, saved/recent/recommended     | — |
| typing-service   | 9007  | `typing`           | WPM/accuracy, daily challenges                  | produces `typing.result` |
| analytics        | 9008  | `analytics`        | performance reports, heatmaps, recommendations  | consumes `exam.completed`, `typing.result`, `lesson.completed` |
| leaderboard      | 9009  | `leaderboard` + Redis | rankings (global/IELTS/SAT/typing), achievements | consumes `exam.completed`, `typing.result` |
| notification     | 9010  | `notification`     | email/SMS/push, system alerts                   | consumes `user.registered`, `exam.completed`, ... |
| file-storage     | 9011  | `files`            | certificates, avatars, documents (S3-compatible)| — |
| admin-service    | 9012  | (aggregates via Feign) | user mgmt, content moderation, dashboards, reports | — |

---

## 3. Data Model (ERD highlights)

Every table carries audit + soft-delete columns:
`id UUID PK`, `created_at`, `updated_at`, `created_by`, `updated_by`,
`deleted boolean default false`, `version bigint` (optimistic locking).

### auth schema
```
accounts(id, email UNIQUE, username UNIQUE, password_hash, status,
         email_verified, provider /*LOCAL|GOOGLE*/, provider_id, ...audit)
roles(id, name UNIQUE /*ROLE_STUDENT|ROLE_TEACHER|ROLE_ADMIN*/, ...audit)
account_roles(account_id FK, role_id FK)            -- M:N
refresh_tokens(id, account_id FK, token_hash, expires_at, revoked, ...audit)
otp_codes(id, account_id FK, code_hash, purpose, expires_at, consumed, ...audit)
```

### users schema
```
profiles(id, account_id /*ref auth.accounts*/, full_name, avatar_url, bio,
         country, role_snapshot, ...audit)
education_history(id, profile_id FK, institution, degree, field, start, end)
certificates(id, profile_id FK, title, issuer, file_id /*ref files*/, issued_at)
portfolio_items(id, profile_id FK, type, title, url, description)
```

### ielts / sat schema (shared shape)
```
exams(id, type, section, title, difficulty, duration_seconds, ...audit)
questions(id, exam_id FK, kind /*MCQ|GAP|ESSAY|SPEAKING*/, prompt, payload jsonb,
          correct_answer, points, ...audit)
exam_attempts(id, account_id, exam_id FK, started_at, submitted_at, status,
              auto_score, manual_score, total_score, ...audit)
attempt_answers(id, attempt_id FK, question_id FK, answer jsonb, awarded_points)
```

Full per-service Flyway migrations live in each service's
`src/main/resources/db/migration`.

---

## 4. Authentication Flow

### Registration + email/OTP verification
```
Client → Gateway → Auth: POST /api/v1/auth/register {email,username,password}
Auth: hash password (BCrypt), persist account(status=PENDING), assign ROLE_STUDENT
Auth → Kafka: publish user.registered
Auth → Notification (via event): send OTP email
Client → Auth: POST /api/v1/auth/verify-otp {email, code}
Auth: account.status=ACTIVE, email_verified=true
```

### Login (password) + token issuance
```
Client → Auth: POST /api/v1/auth/login {usernameOrEmail, password}
Auth: authenticate; issue
      - access  JWT (15m, RS256, claims: sub, roles, type=ACCESS)
      - refresh JWT (7d) — hash stored in refresh_tokens
Client stores tokens; sends "Authorization: Bearer <access>" on each call
```

### Request authorization at the edge
```
Client → Gateway: any /api/** with Bearer access token
Gateway JwtAuthFilter: verify signature (public key) + exp + type=ACCESS
        → inject X-Account-Id, X-Roles headers → forward downstream
Downstream service: trusts gateway headers OR re-verifies JWT (defense in depth)
```

### Refresh + Google OAuth2
```
POST /api/v1/auth/refresh {refreshToken}  → rotate refresh, new access
Google: OAuth2 login → Auth upserts account(provider=GOOGLE) → same token issuance
```

RS256 keypair: private key signs in auth-service; public key (JWKS) distributed to
gateway + services via config-server. Symmetric HS256 is used in the initial build
for simplicity; swap to RS256 before production (see `JwtProperties`).

---

## 5. Standard Service Package Structure

```
com.itshaharcha.<service>
├── <Service>Application.java
├── config/           Security, OpenAPI, Kafka, Redis, Web, Feign config
├── controller/       @RestController — thin, validation + delegation only
├── dto/
│   ├── request/      inbound payloads (@Valid)
│   └── response/     outbound views
├── entity/           JPA @Entity, extends BaseEntity (audit + soft delete)
├── repository/       Spring Data JPA + JpaSpecificationExecutor
├── service/          interfaces
│   └── impl/         implementations (@Transactional)
├── mapper/           MapStruct entity↔dto
├── client/           OpenFeign clients to other services
├── event/            domain events (records)
├── kafka/            producers + @KafkaListener consumers
├── exception/        domain exceptions + @RestControllerAdvice handler
├── security/         filters, JWT utils, method-security
├── specification/    dynamic query/filter builders
├── scheduler/        @Scheduled jobs
├── util/             helpers/constants
└── validation/       custom @Constraint validators
src/main/resources/
├── application.yml
└── db/migration/     Flyway V__*.sql
src/test/java/...     unit (Mockito) + integration (Testcontainers)
```

---

## 6. Deployment Architecture

- **Local dev:** `docker compose` brings up Postgres, Redis, Kafka, Elasticsearch,
  plus infra services (config, discovery, gateway). App services run via IDE or
  their own compose profile.
- **Containers:** each service ships a multi-stage Dockerfile (build → slim JRE).
- **Kubernetes:** Deployment + Service + HPA per microservice; ConfigMap/Secret for
  config; Ingress → gateway. Manifests under `infra/k8s/` (added per service).
- **CI/CD:** GitHub Actions — build + test + Testcontainers on PR; build/push images
  + deploy on main. Workflow under `.github/workflows/`.

---

## 7. Cross-cutting Standards

- **Errors:** RFC-7807 `ApplicationException` → consistent `ErrorResponse` JSON via
  a shared `@RestControllerAdvice` in `common-lib`.
- **Validation:** Jakarta Bean Validation on request DTOs; custom validators in
  `validation/`.
- **Security:** BCrypt(12) password hashing; RBAC via roles in JWT; method-level
  `@PreAuthorize`; rate limiting at gateway (Redis token bucket).
- **Observability:** Spring Boot Actuator + Micrometer; structured JSON logging with
  correlation/trace id propagated through the gateway.
- **Testing:** JUnit 5 + Mockito for units; Testcontainers (Postgres/Kafka) for
  integration; aim for service + repository + controller coverage.

---

## 8. Build Roadmap

Status legend: ✅ done · 🚧 in progress · ⬜ planned

| Phase | Deliverable                                            | Status |
|-------|--------------------------------------------------------|--------|
| 0     | Architecture docs, monorepo scaffold, docker-compose   | ✅ |
| 0     | `common-lib` (BaseEntity, error model, JWT utils)      | ✅ |
| 1     | Authentication Service                                 | ✅ |
| 2     | API Gateway                                            | ✅ |
| 3     | User Service                                           | 🚧 |
| 4     | Admin Service                                          | ⬜ |
| 5     | IELTS Service                                          | ⬜ |
| 6     | SAT Service                                            | ⬜ |
| 7     | IT Learning, Video, Typing                             | ⬜ |
| 8     | Analytics, Leaderboard, Notification, File Storage     | ⬜ |
| 9     | Config + Discovery servers, K8s manifests, CI/CD       | ⬜ |
| 10    | Frontend (Next.js)                                     | ⬜ |

The build deliberately prioritizes a small number of **complete, runnable** services
over a large number of empty stubs.
