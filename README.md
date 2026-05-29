# IT-Shaharcha — Free Education Platform

A modular, microservice-based EdTech platform: IT learning, IELTS & SAT prep, real
exam simulation, typing practice, analytics, leaderboards, and student portfolios.

> **Status:** active build. Foundation + Authentication Service + API Gateway are
> implemented and runnable; remaining services are being added module-by-module.
> See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the full design and the
> [build roadmap](docs/ARCHITECTURE.md#8-build-roadmap).

## Tech stack
Java 21 · Spring Boot 3.4 · Spring Cloud 2024 · PostgreSQL · Redis · Kafka ·
Elasticsearch · Docker · Kubernetes-ready · JWT/OAuth2 · Flyway · MapStruct ·
Testcontainers. Frontend: Next.js + TypeScript + Tailwind + shadcn/ui (planned).

## Repository layout
```
docs/                 architecture & design docs
infra/
  docker/             docker-compose for local infra (Postgres, Redis, Kafka, ES)
services/
  common-lib/         shared: BaseEntity, error model, JWT utilities
  auth-service/       authentication & authorization
  api-gateway/        edge gateway (routing, JWT, rate limiting)
  user-service/       profiles, portfolio, certificates
frontend/             Next.js + TypeScript + Tailwind UI (talks to the gateway)
pom.xml               parent / dependency management
```

## Prerequisites
- JDK 21+ (JDK 25 works)
- Maven 3.9+  (`brew install maven`)
- Docker + Docker Compose
- Node.js 20+ (for the frontend)

## Run the whole thing (frontend + backend)
This is the fastest way to click around the UI and exercise the live backend.

```bash
# 1. Build the service jars (skip tests for speed)
mvn clean install -DskipTests

# 2. Start the full backend stack — Postgres, Redis, auth, user, gateway
#    (jars are mounted, so rebuilds are instant: re-run step 1 then `restart`)
docker compose -f infra/docker/docker-compose.full.yml up -d

# 3. Start the frontend
cd frontend
npm install
npm run dev

# 4. Open the app
#    http://localhost:3000
```

Register an account → you land straight on the dashboard (dev mode auto-activates
accounts, so no email/OTP step). From there you can edit your profile and add
certificates, education, and portfolio items — each call flows browser → Next.js
proxy → API gateway (`:8080`) → auth/user service.

The gateway routes `/api/v1/auth/**` to auth-service and `/api/v1/users|profiles/**`
to user-service. The frontend never calls services directly; `next.config.mjs`
rewrites `/api/*` to the gateway, so there are no CORS issues.

## Run backend services individually
```bash
# Start just the infra (Postgres, Redis, Kafka, Elasticsearch)
docker compose -f infra/docker/docker-compose.yml up -d

mvn clean install
mvn -pl services/auth-service spring-boot:run

# API docs once running:  http://localhost:9001/swagger-ui.html
```

## Module-by-module
Each service is independently buildable (`mvn -pl services/<name>`) and ships its
own Flyway migrations, tests, and Dockerfile. New services follow the package
structure documented in `docs/ARCHITECTURE.md` §5.
