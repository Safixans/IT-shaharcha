# IT-Shaharcha — Free Education Platform

A modular, microservice EdTech platform: IT learning, IELTS & SAT prep, real exam
simulation, typing practice, certificate portfolios, cross-domain analytics, and
leaderboards. Three frontends (public site, learner app, staff console) sit behind a
single API gateway.

> **Status:** all five domain services + gateway + discovery are implemented and
> runnable, and all three frontend apps are built. See
> [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) and [`specs/`](specs/) (the API
> contracts — the source of truth).

## Tech stack
Java 21 (runs on 25) · **Spring Boot 4.0** · **Spring Cloud 2025.1** · PostgreSQL ·
Redis · Kafka · JWT · Flyway · MapStruct · Testcontainers · Micrometer/OTel → Zipkin ·
Prometheus · Zalando Logbook · Logstash. Virtual threads on every servlet service;
the gateway stays reactive (WebFlux). Frontend: **Next.js 16 + React 19 + TypeScript +
Tailwind**, npm-workspaces monorepo.

## Repository layout
```
docs/                 architecture & design docs
specs/                OpenAPI contracts (source of truth) + shared schemas
infra/docker/         compose files, DB init, dev seeds, prometheus config
services/
  common-lib/         shared: BaseEntity, error model, ApiResponse
  eureka-server/      service discovery
  api-gateway/        edge gateway (routing, JWT, rate limiting) — reactive
  identity-service/   accounts, auth/JWT, OTP, profiles, roles          :9003
  learning-service/   tracks/courses/lessons, tutorials, docs, typing   :9004
  assessment-service/ exams, sections/questions, sessions, scoring      :9005
  portfolio-service/  certificates, education, items, published profile :9006
  analytics-service/  event ingest (Kafka + REST), progress, rankings   :9007
frontend/
  packages/           @itsh/auth, @itsh/api-client (shared)
  apps/public/        SSR marketing + auth + published portfolios + rankings  :3002
  apps/console/       teacher authoring + admin management                    :3001
  apps/learner/       the student product                                     :3000
pom.xml               parent / dependency management
```

## Prerequisites
- JDK 21+ (JDK 25 recommended) · Maven 3.9+ · Docker + Compose · Node.js 20+

## Quick start — everything in Docker
```bash
# 1. Build the backend jars (mounted into containers)
mvn clean install -DskipTests

# 2. Bring up backend + observability + all three frontends
docker compose -f infra/docker/docker-compose.full.yml up -d --build

# 3. Seed login accounts (after identity has migrated — ~15s)
docker compose -f infra/docker/docker-compose.full.yml exec -T postgres \
  psql -U itsh -d identity_db < infra/docker/seed-admin.sql
docker compose -f infra/docker/docker-compose.full.yml exec -T postgres \
  psql -U itsh -d identity_db < infra/docker/seed-auth.sql
```

### URLs & dev accounts
| Surface | URL | Login (password `Password123`) |
|---|---|---|
| Learner app | http://localhost:3000 | `student` |
| Console (staff) | http://localhost:3001 | `admin` or `teacher` |
| Public site | http://localhost:3002 | — (register/login) |
| API gateway | http://localhost:8080 | — |
| Eureka | http://localhost:8761 | — |
| Zipkin (traces) | http://localhost:9411 | — |
| Prometheus (metrics) | http://localhost:9090 | — |
| Swagger (per service) | http://localhost:9003/swagger-ui.html | — |

## Alternative — services from Maven, infra in Docker
Faster iteration on a single service:
```bash
docker compose -f infra/docker/docker-compose.yml up -d   # Postgres, Redis, Kafka, ES
mvn -pl services/identity-service spring-boot:run
```

## Frontend without Docker
```bash
cd frontend && npm install
npm run dev -w @itsh/learner    # :3000   (also :public 3002, :console 3001)
```
Each app rewrites `/api/*` → `GATEWAY_URL` (default `http://localhost:8080`), so the
browser is always same-origin (no CORS). The public site's portfolio/leaderboard
pages are server-rendered for SEO.

## Tests
```bash
mvn test                        # backend — full reactor (needs Docker for Testcontainers)
cd frontend && npm test         # frontend — all three apps (vitest)
```

## Notes
- **Auth model:** STUDENT (read) · TEACHER (authoring + cert verify) · ADMIN (all +
  accounts/roles + analytics ingest). Dev mode auto-activates accounts (no OTP).
- **Events/Kafka** run in the full stack (Confluent cp-kafka, KRaft, no ZooKeeper) with
  `KAFKA_ENABLED=true` on every producer + the analytics consumer: actions flow
  producer → `itsh.<service>.events` → analytics, which folds them into progress &
  rankings. Host tools reach the broker at `localhost:29092` (in-network: `kafka:9092`).
  The REST fallback `POST /api/v1/analytics/events:ingest` still works for broker-less
  setups — set `KAFKA_ENABLED=false` to use only that.
- **Observability:** every service exposes `/actuator/prometheus` (scraped by
  Prometheus) and exports traces to Zipkin; `json`/`prod` Spring profiles emit
  structured Logstash JSON logs.
