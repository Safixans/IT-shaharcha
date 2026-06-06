# Assessment Service — User Stories (for redesign)

> **Status:** DRAFT for review. Extracted by Claude; **you (Dilshod) edit / add / cut**,
> then we redesign the service against the agreed set.
>
> **Why we're here:** the current service crams IELTS, SAT, MOCK and PRACTICE into one
> flat model — `Exam → Section(order) → Question(single_choice|…) → scaledScore/maxScore`
> with one synchronous auto-scorer. That cannot represent IELTS (4 skills, audio-once
> listening, ~12 question types, human-graded Writing/Speaking, raw→band conversion) or
> SAT (adaptive modules, 400–1600 scaling). The redesign should treat an **exam type as a
> first-class format** with its own structure, question types, timing and scoring strategy.

## Legend
- **Roles:** `STUDENT` (candidate/test-taker), `TEACHER` (author/examiner/grader), `ADMIN` (platform operator).
- **Applies to:** which exam type(s) a story is relevant for — `ALL`, `IELTS`, `SAT`, `QUIZ` (the generic MOCK/PRACTICE quiz), or `CUSTOM`.
- **Priority:** `M` must-have · `S` should-have · `C` could-have. _(edit freely)_
- IDs: `S-*` student, `T-*` teacher, `A-*` admin, `X-*` cross-cutting, `I-*` IELTS-specific.

---

## 0. Decisions to make first (please answer inline)

> These shape the whole redesign. Edit answers right here.

- **D-1 — One service or split?** Keep a single `assessment-service` that supports multiple
  pluggable formats, or split IELTS into its own module/service? _(recommendation: one)_
  service, format-pluggable; IELTS as a first-class format. **Your call:** _(We will leave it as one service, with different controllers, like for adding reading, writing, listening, grading IELTS practice exam service, sat service for crud, sat service for service exam, grading for sat, generic quiz service simple crud, serving quizzes, grading quizzes. I reckon sat and quiz has a identical structure, if so we can use more generic service under the hood, on top of them having their own services and their own domain.)_
- **D-2 — Which exam types are in scope now?** IELTS (you have a ready solution) + a generic
  QUIZ for MOCK/PRACTICE? Is SAT in scope now or later? _(Your call: IELTS we will serve only listening, reading, writing (we only store user input, not grade it as this a free for all project and we don't have the budjet for ai), their will serve like training service and user won't have the ability to take full exam. As for SAT, QUIZ they are still in the scope)_
- **D-3 — IELTS variant:** Academic, General Training, or both? _(Training)_
- **D-4 — Writing/Speaking grading:** human-only, AI-assisted, or both? Who grades (any
  TEACHER vs. a certified EXAMINER role)? _(Let's have them both. Only teachers can grade reading and live a mark if the student under them. we need to add teacher <- student relationship in identity service and as for AI you can add the functionality using spring ai + openai libraries that gives us ready to use apis, but it won't be active)_
- **D-5 — Speaking delivery:** live examiner, or async (candidate records audio → examiner
  grades later)? _(We won't have this functionality for now)_
- **D-6 — Is "real/graded" IELTS official-style** (full mock under exam conditions) the only
  IELTS mode, or also bite-size **practice by skill / by question-type**? _(More like practice. every listening, reading, writing will have tags for users to sort and they can choose which one they will take)_
- **D-7 — Your ready IELTS solution:** what shape is it in? (DB schema? JSON content format?
  question-type catalog? band tables?) Drop a link/paste so the model matches it: They more like ready to use code. I will leave them here ./ielts-example. Don't take them at a face value. they need some improvements. by the way algorithm doesn't need orderIndex for calculating correct answers so you can ompimize submit body. response should be same like the current one as well.

---

## 1. STUDENT (candidate)

### Epic: Discover & understand exams
- **S-01** `ALL` `M` — As a student, I want to browse available exams filtered by **type**
  (IELTS / SAT / quiz), level, and skill, so I can find the right practice. And I want IELTS exams divided by writing, listening, reading. I want them seperated.
- **S-02** `ALL` `M` — As a student, I want each exam to show its **format up front**
  (sections, number of questions, total time, whether it's timed/graded/practice) so I know
  what I'm starting.
- **S-03** `IELTS` `S` — As a student, I want to see whether an IELTS test is **Academic or
  General Training** and **full-test vs single-skill**, so I pick the correct one.

### Epic: Take an exam (the core flow)
- **S-04** `ALL` `M` — As a student, I want to **start an attempt** and get a session that
  tracks my progress, so I can take the exam and resume if I disconnect. 
- **S-05** `ALL` `M` — As a student, I want **per-section and overall timers** with a visible
  countdown, and **auto-submit when time expires**, so the test reflects real conditions.
- **S-06** `ALL` `M` — As a student, I want my answers **auto-saved continuously**, so a
  refresh/crash never loses work.
- **S-07** `IELTS` `M` — As a student taking **Listening**, I want the audio to **play once,
  automatically**, with no pause/seek, while I answer the questions for that part, so it
  mirrors the real exam. _(timing tied to the audio, not a free clock + 1 minute for review)_
- **S-08** `IELTS`/`SAT` `M` — As a student, I want **reading passages / source material
  shown alongside** their questions (split view), so I can reference while answering.
- **S-09** `ALL` `M` — As a student, I want to **navigate between questions/sections** (next,
  back, jump, "flag for review", see answered/unanswered map) within the rules of the exam
  (e.g. IELTS forbids going back to a finished listening part), so I can manage my time.
- **S-10** `IELTS` `M` — As a student in **Writing**, I want a text editor with a **live word
  count** and the task prompt (and Task-1 image/chart) visible, so I can write Task 1 & Task 2.
- **S-11** `ALL` `M` — As a student, I want to **submit** the attempt (or have it auto-submit)
  and get a clear confirmation, so I know it's done.

### Epic: Question-type interactions (rendering & answering)
- **S-13** `IELTS` `M` — As a student, I want to answer the **full IELTS question-type set**
  correctly: multiple choice (single & multi), matching (headings/information/features),
  True/False/Not Given, Yes/No/Not Given, gap/sentence/summary/note/table/flow-chart
  completion, plan/map/diagram labelling, short answer. _(see §4 for the catalog)_
- **S-14** `QUIZ` `M` — As a student, I want simple objective questions (single/multiple
  choice, true/false, short answer) for MOCK/PRACTICE quizzes, scored instantly.
- **S-15** `ALL` `S` — As a student, I want **answer formats validated as I go** (e.g. "ONE
  WORD ONLY", numeric only), so I don't lose marks on format mistakes.

### Epic: Results & feedback
- **S-16** `IELTS` `M` — As a student, I want my result as a **band score per skill (0–9, half
  bands) and an overall band**, computed from the official **raw→band conversion**, not a raw
  percentage, so it's meaningful.
- **S-17** `SAT` `S` — As a student, I want SAT results as **section + total scaled scores
  (e.g. 400–1600)**, not a percentage.
- **S-18** `QUIZ` `M` — As a student, I want quiz results as score/percentage with correct
  answers and explanations, immediately.
- **S-19** `IELTS` `M` — As a student, I want **objective skills (Listening/Reading) scored
  automatically** but **Writing/Speaking marked "pending" until graded**, then notified when
  the band is ready, so I understand the wait.
- **S-20** `IELTS` `S` — As a student, I want **examiner feedback against the band
  descriptors** (Task Achievement, Coherence/Cohesion, Lexical Resource, Grammar; Fluency,
  Pronunciation for speaking), so I know how to improve.
- **S-21** `ALL` `S` — As a student, I want **per-question review** after scoring (my answer
  vs. correct, with the passage/audio reference), so I can learn from mistakes.
- **S-22** `ALL` `M` — As a student, I want my **attempt history and best/latest scores**, so
  I can track progress over time.
- **S-23** `ALL` `C` — As a student, I want a **practice mode** (no timer / instant per-question
  feedback / by question-type) distinct from a **real/mock mode** (timed, scored at end).

---

## 2. TEACHER (author / examiner / grader)

### Epic: Authoring exams by format
- **T-01** `ALL` `M` — As a teacher, I want to create an exam **from a format template**
  (IELTS Academic, IELTS GT, SAT, blank quiz) so the correct structure (skills, parts,
  timing rules, scoring) is enforced — not a freeform "section/question" blob.
- **T-02** `IELTS` `M` — As a teacher, I want to author an IELTS test as **4 skills →
  parts/passages → question groups → questions**, where each group has its own **instructions
  and question type**, so the structure matches the real exam.
- **T-03** `IELTS` `M` — As a teacher, I want to **attach media**: Listening audio per part,
  Reading passages, Task-1 charts/images, so candidates have the source material.
- **T-04** `IELTS` `M` — As a teacher, I want to define **answer keys per question type**
  (incl. acceptable alternates/synonyms, case/space rules, "ONE WORD" limits) so
  auto-marking of Listening/Reading is accurate.
- **T-05** `ALL` `M` — As a teacher, I want to set **timing rules** (per section/part; audio
  duration drives Listening) and **navigation rules** (can/can't go back), per format.
- **T-06** `QUIZ` `M` — As a teacher, I want a lightweight **quiz builder** (objective
  questions + answer key) for MOCK/PRACTICE, without the IELTS scaffolding.
- **T-07** `ALL` `S` — As a teacher, I want to **import an exam from a structured file**
  (your ready IELTS content format / JSON) so I can bulk-load tests instead of clicking. _(ties to D-7)_
- **T-08** `ALL` `S` — As a teacher, I want a **reusable question/passage bank** so I can
  assemble new tests from existing items.
- **T-09** `ALL` `S` — As a teacher, I want to **preview an exam exactly as a candidate sees
  it** (incl. audio/timer) before publishing.
- **T-10** `ALL` `M` — As a teacher, I want **draft → published → archived** lifecycle with
  **versioning**, so editing a live exam doesn't corrupt in-flight attempts or past results.
- **T-** `M` - As a teacher, I want to have check button for exams that will make them active/deactive. of cource it should be taken into considiration that for activating an exam it should have correct amount of questions, like listening - 40, reading - 13/14, writing 1 - with image. as for sat i don't really know which part has how many questions. look it up. as for deactivations, it shouldn't corrupt already started exams, so take that into consideration as well.

### Epic: Scoring configuration
- **T-11** `IELTS` `M` — As a teacher/admin, I want to manage the **raw→band conversion table**
  per skill (and per test if needed), so auto-scored skills map to bands correctly.
- **T-12** `SAT` `S` — As a teacher, I want to configure **scaled-score curves** for SAT.
- **T-13** `ALL` `S` — As a teacher, I want per-question **marks/weights** and partial-credit
  rules (e.g. multi-select), so scoring is fair.

### Epic: Grading subjective work
- **T-14** `IELTS` `M` — As an examiner, I want a **grading queue** of pending Writing &
  Speaking submissions, so I can find work to mark.
- **T-15** `IELTS` `M` — As an examiner, I want to score Writing/Speaking **against the four
  band criteria each**, leave comments/annotations, and the system computes the skill band,
  so grading is consistent and traceable.
- **T-16** `IELTS` `S` — As an examiner, I want to play back the candidate's **Speaking audio**
  and grade per prompt.
- **T-17** `IELTS` `C` — As an examiner, I want **AI-assisted draft scoring/feedback** I can
  accept or override, to speed up marking. _(depends on D-4)_
- **T-18** `ALL` `S` — As a teacher, I want to **review/override an auto-scored result** when a
  candidate appeals or a key was wrong.

### Epic: Insight
- **T-19** `ALL` `S` — As a teacher, I want **item analytics** (per-question difficulty,
  discrimination, most-missed) so I can improve my tests.
- **T-20** `ALL` `C` — As a teacher, I want **cohort results** for exams I authored, so I can
  see how students perform.

---

## 3. ADMIN (platform operator)

- **A-01** `ALL` `M` — As an admin, I want to manage the **catalog of exam formats/types** and
  enable/disable them, so the platform can grow (add TOEFL later) without code forks per type.
- **A-02** `ALL` `M` — As an admin, I want to manage **roles for grading** (who can be an
  examiner) and assign graders to queues, so subjective marking is controlled. _(ties to D-4)_
- **A-03** `ALL` `S` — As an admin, I want to configure **global policies**: attempt limits,
  retake cooldowns, time multipliers (accessibility), proctoring on/off.
- **A-04** `ALL` `S` — As an admin, I want **moderation**: unpublish/flag a bad exam or item,
  and reset/invalidate a compromised attempt.
- **A-05** `ALL` `S` — As an admin, I want **scoring schemes & band tables versioned and
  audited**, so result computation is reproducible and defensible.
- **A-06** `ALL` `S` — As an admin, I want **platform analytics** (attempts, completion rates,
  score distributions by exam type) feeding the analytics service.
- **A-07** `ALL` `C` — As an admin, I want **data export** (results, transcripts) for reporting/compliance.

---

## 4. IELTS deep dive (you have a ready solution — align to it)

### Structure (the model must represent this)
- **Test** → **Skill** (Listening, Reading, Writing, Speaking) → **Part/Passage/Task** →
  **Question group** (shared instruction + one question type + optional shared media/answer
  options) → **Question** → **Answer key**.
- A skill can be taken **standalone** (single-skill practice) or as part of a **full test**.

### Listening (`I-L*`)
- **I-L1** `M` — 4 parts, **single automatic play**, ~40 questions; types: form/note/table/
  flow-chart/summary completion, multiple choice, matching, plan/map/diagram labelling,
  sentence completion, short answer.
- **I-L2** `M` — transfer/finalize answers; auto-marked with alternates & word-limit rules.

### Reading (`I-R*`)
- **I-R1** `M` — 3 passages, 40 questions; Academic vs GT differ. Types: T/F/Not Given,
  Yes/No/Not Given, matching headings/information/features/sentence-endings, MCQ, all
  completion types, short answer, diagram labelling.
- **I-R2** `M` — passage shown beside questions; auto-marked.

### Writing (`I-W*`)
- **I-W1** `M` — Task 1 (Academic: describe visual / GT: letter) + Task 2 (essay); word counts
  (≥150 / ≥250); Task 2 weighted heavier.
- **I-W2** `M` — examiner grades each task on 4 criteria → task band → skill band; **human (or
  AI-assisted) graded, asynchronous**.

### Speaking (`I-S*`)
- **I-S1** `S` — 3 parts (intro/Q&A, cue-card long turn, discussion); prompts delivered;
  candidate **records audio** (or live).
- **I-S2** `S` — examiner grades on Fluency & Coherence, Lexical Resource, Grammatical Range &
  Accuracy, Pronunciation → skill band.

### Banding (`I-B*`)
- **I-B1** `M` — Listening & Reading: **raw (0–40) → band** via conversion table (table can
  differ Academic vs GT and per test).
- **I-B2** `M` — Writing & Speaking: criteria → skill band (whole/half).
- **I-B3** `M` — **Overall band = mean of the four skill bands, rounded to the nearest half
  band** (with the official .25/.75 rounding rule).

---

## 5. Cross-cutting (`X-*`)
- **X-01** `M` — **Pluggable scoring strategy** per exam type (auto-objective, band-table,
  scaled-curve, criteria-based human) — no single hardcoded scorer.
- **X-02** `M` — **Polymorphic question model** (a closed catalog of question types, each with
  its own payload + answer-key + marking rule) instead of one `single_choice` shape.
- **X-03** `M` — **Attempt/session integrity**: resume, autosave, server-authoritative timers,
  auto-submit, idempotent submit, no answer leakage (correct answers never sent to client
  mid-attempt).
- **X-04** `M` — **Media handling**: audio (listening), images (Task 1, maps/diagrams),
  candidate audio uploads (speaking) — storage + signed access.
- **X-05** `S` — **Events**: emit `assessment.attempt.started/submitted/scored`,
  `assessment.writing.graded`, `assessment.band.assigned` to analytics (per-skill + overall).
- **X-06** `S` — **Versioning & immutability of results**: a scored attempt is tied to the
  exam version + scoring scheme used, so historical results never change.
- **X-07** `C` — **Accessibility**: extra-time multipliers, larger fonts, captions/transcripts.
- **X-08** `C` — **Anti-cheat/proctor hooks**: tab-switch logging, single-active-attempt, time
  windows.

---

## 6. What's explicitly NOT this service (boundaries)
- Identity/roles → identity-service. Cross-domain points/rankings → analytics-service
  (we just emit events). Course/lesson content → learning-service. Certificates of
  achievement → portfolio-service.
- Need to add additional functionality to identity service where we will have teacher <- student relationship which are divided by groups. As you know group, user domains must be independent. so we need an additional domain to add fields teacher, student, group, but groups are assigend to teachers so maybe they are inside group? either way teachers only can grade their students. we need this. and to adding groups are done by moderator or admin, while adding students are done by teacher(can be done by moderator, admin). if student already part of other group it should give an error.

---

## 7. Your additions / edits
> Add stories, change priorities, strike anything you don't want. Anything here wins over
> the draft above.

- Uploading and Downloading files: I think we need some kind of universal interface for that. I was thinking maybe use minio like we upload it, and we have minio create a presidned url for download. we can use gateway for redirectiring. let's say /api/upload/**||/api/download/** sends it to dedicated attachemnt service. /api/download/** returns presigned url from minio that works for 10 minuts it should start with http://domain/{presigned_url with some additional path for indetification} to http://minio(from docker compose)/{presigned_url}. and we need to stream audio files for simingles experiences as well. 

- (add yours…)
