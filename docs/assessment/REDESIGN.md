# Assessment Redesign — Design (v1)

> Derived from `USER_STORIES.md` + the `ielts-example/` reference. **Approved decisions are
> baked in;** a few **[ASSUMPTION]** items are flagged for a final confirm before code.
> No code is written until this doc is signed off.

## 1. Scope

**In:** modular, single-unit **training** (no full exams, no adaptive, no speaking).
- **IELTS** — Listening, Reading, Writing units (HTML-blot content, jsonb answer key).
- **SAT** — modular: one unit = one module (R&W ~27 Q / Math ~22 Q), objective.
- **QUIZ** — generic objective practice (MOCK/PRACTICE).

**Out:** speaking, full multi-skill IELTS exams, SAT adaptive routing, AI auto-grading
(scaffolded, inactive).

**Engines (2) over one attempt core:**
- `ielts` — rich HTML content with embedded blots; value-based grading; per-skill rules.
- `objective` — flat question list; QUIZ + SAT modules sit on top (SAT adds a scaled curve).
- `attempt` (shared) — records, timing, autosave, resume, snapshotting.

## 2. Confirmed decisions
- D-1 one `assessment-service`, many controllers; SAT+QUIZ share the objective engine.
- D-2 modular training only; **SAT also modular** (like IELTS). Writing **stored, not auto-graded**.
- D-3 IELTS = **General Training** content variant.
- D-4 **writing graded by the student's teacher (human)**; AI via Spring AI + OpenAI **scaffolded, inactive**.
- D-6 units carry **tags**; students filter/choose.
- 4 interaction primitives only: **INPUT, RADIO, SELECT, MULTI_SELECT** (configurable `correctCount`).
- **Problems stored as jsonb, immutable; edit = re-parse + overwrite.** Attempt snapshots content at start.
- Activation gates: **listening 40 Q, reading 13/14 Q, writing Task-1 needs an image** (SAT module counts in §7).
- Writing distinguishes **Task 1 (image, ≥150 words)** and **Task 2 (essay, ≥250 words)**.
- Submit body **drops orderIndex**; responses keep the platform **`ApiResponse` envelope**.

### [ASSUMPTION] result shapes (confirm)
- **Listening** unit (40 Q) → `raw (0–40)` **+ band** via raw→band table.
- **Reading** unit (single passage, 13/14 Q) → `raw + %` (no official band — bands need full 40). Optional indicative band table later.
- **Writing** → teacher **band 0–9** + per-criterion comments; "pending" until graded.
- **SAT module** → `raw + %` (optional scaled later; a single module isn't officially scalable).
- **QUIZ** → `raw + %` + correct answers/explanations immediately.

## 3. Timing contract (kills the timezone bug)
Root cause of the old bug: `LocalDateTime` (no zone) persisted + client did `clientNow() − endTime`.
**Rules:**
- Persist all times as **`Instant` (UTC)** → `timestamptz`. Serialize ISO-8601 `…Z`.
- Every start/resume/exam response returns: `startedAt`, `endsAt` (display only), **`serverNow`**,
  and **`remainingSeconds` = max(0, endsAt − serverNow)** (server-computed).
- **Client never diffs its own clock against a server timestamp** — it counts down from
  `remainingSeconds` with a monotonic timer; re-fetch to re-sync.
- Server is authoritative on submit. With **autosave**, an expired/late submit scores what was
  saved (no wipe).
- Listening: `endsAt = startedAt + audioLength + reviewBuffer(60s)`. Reading/Writing/SAT/Quiz:
  `startedAt + unit.durationSeconds`.

## 4. Domain model

### 4.1 Unit (content) — table per family, jsonb answer key
**`ielts_unit`**
| col | type | notes |
|---|---|---|
| id | uuid pk | |
| skill | varchar | LISTENING / READING / WRITING |
| writing_task | int null | 1 or 2 (writing only) |
| title | varchar | |
| tags | text (jsonb array) | filtering |
| original_section_data | text | raw authored HTML |
| section_data | text | answer-stripped HTML served to students |
| passage | text null | reading |
| problems | jsonb | answer key (see 4.2); **null for writing** |
| problem_count | int | derived |
| audio_attachment_id | uuid null | listening |
| image_attachment_id | uuid null | writing task 1 |
| duration_seconds | int | non-listening |
| active | boolean | gated (§7) |
| + BaseEntity (audit, version, deleted) | | |

**`objective_unit`** (QUIZ + SAT)
`id, kind (QUIZ|SAT), sat_module (RW|MATH|null), title, tags, durationSeconds, questions jsonb (answer key), questionCount, scaledCurveId null, active, +BaseEntity`.

### 4.2 `problems` jsonb (IELTS) — answer key, server-only
```json
[{ "problemId":"uuid", "type":"INPUT|RADIO|SELECT|MULTI_SELECT",
   "correctCount":1, "correctOptions":["you","YOU","You"] }]
```
- INPUT: `correctOptions` = acceptable answers (the `/`-split alternates).
- RADIO/SELECT: `correctOptions` = the one correct value(s).
- MULTI_SELECT: `correctOptions` = N correct values, `correctCount=N`.
The **served** payload (`section_data` HTML + a problems list) carries **no correct flags/counts**.

### 4.3 Attempt (record)
**`attempt`**: `id, student_id, unit_id, family (IELTS_LISTENING|IELTS_READING|IELTS_WRITING|SAT|QUIZ),
status (IN_PROGRESS|COMPLETED|PENDING_GRADING|GRADED|EXPIRED), title,
snapshot_section_data text, snapshot_problems jsonb (key copied at start),
answers jsonb, essay text null, correct int, incorrect int, raw int, band numeric(2,1) null,
score_percent numeric null, started_at, ends_at, submitted_at, graded_at, graded_by null, +BaseEntity`.
- One **IN_PROGRESS** attempt per (student, family): resume if live, else auto-submit the stale one.

### 4.4 `answers` jsonb (graded result, per attempt)
```json
[{ "problemId":"uuid", "submitted":["FALSE"], "correctOptions":["FALSE"], "correct":true }]
```

## 5. Grading
- **Auto (listening/reading/quiz/SAT):** value-based, **normalized** (trim + case-insensitive +
  collapse internal whitespace). Single: `submitted ∈ correctOptions`. MULTI_SELECT: pool-match
  submitted values vs the N correct (matched→correct, surplus→incorrect). Reads the **attempt's
  snapshot key**, not the live unit.
- **IELTS listening band:** raw(0–40) → band via a `band_table` (skill-scoped, editable).
- **Writing:** auto status `PENDING_GRADING`; the **teacher of the student** sets band (0–9) +
  4-criteria comments → `GRADED`. Enforced by identity's teacher↔student relation.
- **SAT/QUIZ:** raw + %.

## 6. API (controllers per the "many controllers" decision)
Base `/api/v1/assessment`. All list endpoints support `tags`, paging.
- **IELTS listening:** `GET /ielts/listening` (filter by tags), `GET /{id}`, `POST /{id}:start`,
  `POST /attempts/{attemptId}:autosave`, `POST /attempts/{attemptId}:submit`,
  `GET /attempts/{attemptId}` (report), `GET /attempts` (history). Author: `POST/PUT /admin/ielts/listening`,
  `POST /admin/ielts/listening/{id}:activate|deactivate`, `DELETE …`.
- **IELTS reading / writing:** same shape (`/ielts/reading`, `/ielts/writing`). Writing submit
  stores essay → PENDING_GRADING.
- **SAT:** `/sat/modules…` (objective engine). **QUIZ:** `/quizzes…`.
- **Grading (teacher):** `GET /grading/queue` (writing of *my* students), `POST /grading/{attemptId}`
  (band + criteria + comments).
- Docs/Swagger as today; gateway route unchanged (`/api/v1/assessment/**`).

### 6.1 Submit (optimized — no orderIndex)
```json
{ "answers": [ { "problemId": "uuid", "values": ["snails"] },
               { "problemId": "uuid", "values": ["Hey","Hi"] },
               { "problemId": "uuid", "values": [] } ] }
```
Response: `ApiResponse<AttemptReport>` (same envelope as the rest of the platform).

## 7. Authoring & activation
- Teacher authors HTML with blots (`input[value]`, `select-blot`, `radio-blot`,
  `checkbox-blot[data-correct-options]`). On save: **Jsoup parse → build `problems` jsonb +
  strip markers → store `section_data`**. Re-edit = re-parse + overwrite (delete+recreate semantics).
- Harden over the example: configurable `correctCount` (not hardcoded 2), validate on save,
  don't leak counts in served payload.
- **Activation gates** (`:activate` requires): listening `problemCount==40`; reading
  `problemCount ∈ {13,14}`; writing Task-1 has `image_attachment_id`; SAT R&W module `==27`,
  Math `==22`; QUIZ `>=1`. **Deactivate never touches IN_PROGRESS attempts** (they ran off a snapshot).

## 8. Identity additions (prereq) — groups & relationships
New, **independent domain** in identity-service (kept out of the account domain):
- `MODERATOR` role added.
- `group` (id, name, teacher_id, created_by). Groups created by **moderator/admin**.
- `group_membership` (group_id, student_id **unique on student_id** → one group per student;
  adding a student already in a group → `409`). Students added by the **teacher** (or mod/admin).
- New endpoints + an internal lookup `isStudentOf(teacherId, studentId)` (or `GET
  /identity/teachers/{id}/students`) so assessment can authorize writing grading.

## 9. Attachment-service (new) + MinIO
- New service + MinIO. Gateway: `/api/upload/**`, `/api/download/**` → attachment-service.
- Upload → store in MinIO, return a fileId. `GET /api/download/{id}` → **presigned URL (10 min)**
  with the MinIO host rewritten to the public domain; **audio is streamed** (range requests) for
  seamless listening playback.
- Assessment stores only attachment **ids**; IELTS audio/images reference them.

## 10. Migration & blast radius
- **Replaces** today's `Exam / Section / Question / ExamSession / SessionAnswer / ExamResult`
  + their controllers/services. New Flyway migration; old tables dropped (no prod data).
- **Frontend:** learner exam pages + console assessment builder + `@itsh/api-client` assessment
  methods are rewritten to the new contracts (HTML rendering for IELTS, the optimized submit,
  the `remainingSeconds` timer).
- **Events:** emit `assessment.attempt.started/submitted/scored`, `assessment.writing.graded`
  to analytics (per-family).

## 11. Proposed build order
1. **identity:** groups + membership + `MODERATOR` + teacher-of lookup.
2. **attachment-service** + MinIO + gateway routes + streaming.
3. **assessment rewrite:** attempt core + timing → objective engine (QUIZ, SAT) → IELTS engine
   (parser, listening/reading auto-grade, writing store) → teacher grading queue.
4. **frontend:** learner (HTML-rendered IELTS player, audio stream, countdown) + console authoring
   + api-client.

## 12. Open / confirm
- The **[ASSUMPTION]** result shapes in §2.
- §8 identity contract: is `isStudentOf` internal-only, or a public teacher-roster endpoint too?
- Reading unit = exactly one passage (13/14), correct?
