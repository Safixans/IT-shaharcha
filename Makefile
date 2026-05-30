# IT-Shaharcha — common dev workflows.
# Usage: `make <target>`. The full stack assumes Docker + Compose are running.

COMPOSE      := docker compose -f infra/docker/docker-compose.full.yml
COMPOSE_INFRA := docker compose -f infra/docker/docker-compose.yml
PSQL         := $(COMPOSE) exec -T postgres psql -U itsh -d identity_db

.PHONY: help build up down restart logs ps seed infra test test-be test-fe fe-install clean

help:
	@echo "build      Build backend jars (mvn clean install -DskipTests)"
	@echo "up         Build jars + start the full stack (backend + observability + 3 UIs)"
	@echo "seed       Seed admin/teacher + student login accounts"
	@echo "down       Stop the full stack"
	@echo "restart    Restart the backend services (after a rebuild)"
	@echo "logs       Tail all container logs"
	@echo "ps         Show running containers"
	@echo "infra      Start backing infra only (Postgres, Redis, Kafka, ES)"
	@echo "test       Run backend + frontend tests"
	@echo "clean      Stop the stack and remove volumes (wipes data)"

build:
	mvn clean install -DskipTests

up: build
	$(COMPOSE) up -d --build

down:
	$(COMPOSE) down

restart:
	$(COMPOSE) restart eureka-server identity-service learning-service assessment-service portfolio-service analytics-service api-gateway

logs:
	$(COMPOSE) logs -f

ps:
	$(COMPOSE) ps

seed:
	$(PSQL) < infra/docker/seed-admin.sql
	$(PSQL) < infra/docker/seed-auth.sql

infra:
	$(COMPOSE_INFRA) up -d

fe-install:
	cd frontend && npm install

test: test-be test-fe

test-be:
	mvn test

test-fe:
	cd frontend && npm test

clean:
	$(COMPOSE) down -v
