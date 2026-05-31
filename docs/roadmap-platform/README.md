# Roadmap Platform — Design Dossier

Reverse-engineering analysis of **roadmap.sh** and the system design for an
IT-Shaharcha roadmap ecosystem (visualizer, progress, search, admin builder, AI).
Curriculum content stays **original**; only the visual/interaction patterns are
reproduced. Read in order:

1. [00 — Research Report](00-research-report.md) — IA, design system, UX,
   interactions, technical & roadmap-engine analysis; gap vs. current platform.
2. [01 — Architecture](01-architecture.md) — service boundaries, graph engine,
   progress, search, state management, AI service.
3. [02 — Database](02-database.md) — Flyway DDL for the roadmap graph, progress,
   FTS, AI schema; seeding/migration from the current static file.
4. [03 — API](03-api.md) — public catalog, progress, search, admin authoring, AI;
   error model; `@itsh/api-client` surface.
5. [04 — Components](04-components.md) — `@itsh/roadmap` package + learner/console/
   public component trees and data flow.
6. [05 — Implementation Plan](05-implementation-plan.md) — incremental, vertically
   sliced milestones with acceptance gates.

> Extends `docs/ARCHITECTURE.md` (platform source of truth). No code is written
> until this dossier is reviewed; recommended first build is Milestones 0+1.
