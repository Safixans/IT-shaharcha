"use client";

import { use, useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import {
  api,
  ApiError,
  type AnswerInput,
  type AttemptFamily,
  type AttemptReport,
  type AttemptSession,
  type ServedProblem,
} from "@itsh/api-client";
import { ErrorBanner, PageHeader } from "../../../../components/ui";
import { BlotForm, type AnswerMap } from "../../../../components/BlotForm";

type Phase = "intro" | "taking" | "done";

const FAMILY_LABEL: Record<AttemptFamily, string> = {
  IELTS_LISTENING: "IELTS Listening",
  IELTS_READING: "IELTS Reading",
  IELTS_WRITING: "IELTS Writing",
  SAT: "SAT",
  QUIZ: "Quiz",
};

const msg = (e: unknown, fallback: string) => (e instanceof ApiError ? e.message : fallback);

export default function TakeUnitPage({ params }: { params: Promise<{ unitId: string }> }) {
  const { unitId } = use(params);
  const family = (useSearchParams().get("family") as AttemptFamily | null) ?? null;

  const [phase, setPhase] = useState<Phase>("intro");
  const [session, setSession] = useState<AttemptSession | null>(null);
  const [report, setReport] = useState<AttemptReport | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [answers, setAnswers] = useState<AnswerMap>({});
  const [essay, setEssay] = useState("");

  const setAnswer = useCallback(
    (problemId: string, values: string[]) => setAnswers((a) => ({ ...a, [problemId]: values })),
    [],
  );

  const toAnswers = useCallback(
    (): AnswerInput[] =>
      Object.entries(answers)
        .filter(([, v]) => v.length > 0)
        .map(([problemId, values]) => ({ problemId, values })),
    [answers],
  );

  const isWriting = family === "IELTS_WRITING";

  async function start() {
    if (!family) {
      setError("Missing unit family.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const s = await api.startAttempt(family, unitId);
      setSession(s);
      setPhase("taking");
    } catch (err) {
      setError(msg(err, "Could not start. The unit may not be available yet."));
    } finally {
      setBusy(false);
    }
  }

  const submit = useCallback(async () => {
    if (!session) return;
    setBusy(true);
    setError(null);
    try {
      const r = await api.submitAttempt(session.attemptId, {
        answers: toAnswers(),
        essay: isWriting ? essay : undefined,
      });
      setReport(r);
      setPhase("done");
    } catch (err) {
      setError(msg(err, "Could not submit."));
    } finally {
      setBusy(false);
    }
  }, [session, toAnswers, essay, isWriting]);

  if (!family) {
    return (
      <>
        <PageHeader title="Training" action={<Link href="/exams" className="btn-ghost">← Back</Link>} />
        <ErrorBanner message="Open a unit from the training library." />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={session?.title ?? FAMILY_LABEL[family]}
        description={FAMILY_LABEL[family]}
        action={
          <Link href="/exams" className="btn-ghost">
            ← Training
          </Link>
        }
      />
      {error && (
        <div className="mb-4">
          <ErrorBanner message={error} />
        </div>
      )}

      {phase === "intro" && (
        <div className="card">
          <p className="text-sm text-slate-600">
            {isWriting
              ? "Write your response within the time limit. A teacher will grade it and return a band."
              : "Answers are scored as soon as you submit. The timer counts down server-side — your progress autosaves."}
          </p>
          <button className="btn-primary mt-4" onClick={start} disabled={busy}>
            {busy ? "Starting…" : "Start"}
          </button>
        </div>
      )}

      {phase === "taking" && session && (
        <AttemptRunner
          session={session}
          answers={answers}
          essay={essay}
          busy={busy}
          isWriting={isWriting}
          onAnswer={setAnswer}
          onEssay={setEssay}
          onAutosave={() =>
            api
              .autosaveAttempt(session.attemptId, {
                answers: toAnswers(),
                essay: isWriting ? essay : undefined,
              })
              .catch(() => {})
          }
          onSubmit={submit}
        />
      )}

      {phase === "done" && report && <Report report={report} />}
    </>
  );
}

function AttemptRunner({
  session,
  answers,
  essay,
  busy,
  isWriting,
  onAnswer,
  onEssay,
  onAutosave,
  onSubmit,
}: {
  session: AttemptSession;
  answers: AnswerMap;
  essay: string;
  busy: boolean;
  isWriting: boolean;
  onAnswer: (problemId: string, values: string[]) => void;
  onEssay: (v: string) => void;
  onAutosave: () => void;
  onSubmit: () => void;
}) {
  const [remaining, setRemaining] = useState(session.timing.remainingSeconds);
  const [audioUrl, setAudioUrl] = useState<string | null>(null);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const submittedRef = useRef(false);
  const submitRef = useRef(onSubmit);
  submitRef.current = onSubmit;

  // Countdown from the server-computed remainingSeconds — never diff the client clock.
  useEffect(() => {
    const id = setInterval(() => setRemaining((r) => Math.max(0, r - 1)), 1000);
    return () => clearInterval(id);
  }, []);

  // Auto-submit once when time runs out.
  useEffect(() => {
    if (remaining <= 0 && !submittedRef.current) {
      submittedRef.current = true;
      submitRef.current();
    }
  }, [remaining]);

  // Autosave every 20s.
  useEffect(() => {
    const id = setInterval(onAutosave, 20000);
    return () => clearInterval(id);
  }, [onAutosave]);

  // Resolve media (presigned URLs) for listening audio / writing image.
  useEffect(() => {
    if (session.audioId) api.attachmentUrl(session.audioId).then(setAudioUrl).catch(() => {});
    if (session.imageId) api.attachmentUrl(session.imageId).then(setImageUrl).catch(() => {});
  }, [session.audioId, session.imageId]);

  const mm = String(Math.floor(remaining / 60)).padStart(2, "0");
  const ss = String(remaining % 60).padStart(2, "0");
  const low = remaining <= 60;

  return (
    <div className="space-y-4">
      <div className="sticky top-0 z-10 -mx-1 flex items-center justify-between rounded-lg border border-slate-200 bg-white/90 px-4 py-2 backdrop-blur">
        <span className="text-xs text-slate-500">Time remaining</span>
        <span className={`font-mono text-lg font-semibold ${low ? "text-red-600" : "text-slate-900"}`}>
          {mm}:{ss}
        </span>
      </div>

      {audioUrl && (
        <div className="card">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-brand-600">Audio</p>
          <audio src={audioUrl} controls className="w-full" />
        </div>
      )}

      {session.passage && (
        <div className="card whitespace-pre-wrap text-sm leading-relaxed text-slate-700">
          {session.passage}
        </div>
      )}

      {imageUrl && (
        <div className="card">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={imageUrl} alt="Task 1 visual" className="mx-auto max-h-96 rounded" />
        </div>
      )}

      {session.prompt && (
        <div className="card text-sm leading-relaxed text-slate-800">
          <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-brand-600">Task</p>
          {session.prompt}
        </div>
      )}

      {/* IELTS Listening/Reading: interactive HTML with blots. */}
      {session.sectionData && (
        <div className="card">
          <BlotForm html={session.sectionData} answers={answers} onChange={onAnswer} disabled={busy} />
        </div>
      )}

      {/* Objective families: served problems as controls. */}
      {session.problems.length > 0 && !session.sectionData && (
        <div className="space-y-3">
          {session.problems.map((p, i) => (
            <ProblemCard
              key={p.problemId}
              index={i + 1}
              problem={p}
              value={answers[p.problemId] ?? []}
              onChange={(v) => onAnswer(p.problemId, v)}
              disabled={busy}
            />
          ))}
        </div>
      )}

      {/* Writing: essay. */}
      {isWriting && (
        <div className="card">
          <textarea
            className="input min-h-80"
            value={essay}
            onChange={(e) => onEssay(e.target.value)}
            placeholder="Write your response here…"
            disabled={busy}
          />
          <p className="mt-1 text-xs text-slate-400">
            {essay.trim().split(/\s+/).filter(Boolean).length} words
          </p>
        </div>
      )}

      <button className="btn-primary" onClick={onSubmit} disabled={busy}>
        {busy ? "Submitting…" : "Submit"}
      </button>
    </div>
  );
}

function ProblemCard({
  index,
  problem,
  value,
  onChange,
  disabled,
}: {
  index: number;
  problem: ServedProblem;
  value: string[];
  onChange: (v: string[]) => void;
  disabled?: boolean;
}) {
  const opts = problem.options ?? [];
  return (
    <div className="card">
      <p className="font-medium text-slate-900">
        {index}. {problem.prompt}
      </p>
      <div className="mt-3 space-y-2">
        {problem.type === "INPUT" && (
          <input
            className="input"
            value={value[0] ?? ""}
            disabled={disabled}
            onChange={(e) => onChange(e.target.value ? [e.target.value] : [])}
            placeholder="Your answer…"
          />
        )}
        {problem.type === "SELECT" && (
          <select
            className="select"
            value={value[0] ?? ""}
            disabled={disabled}
            onChange={(e) => onChange(e.target.value ? [e.target.value] : [])}
          >
            <option value="">—</option>
            {opts.map((o) => (
              <option key={o.id} value={o.text}>
                {o.text}
              </option>
            ))}
          </select>
        )}
        {(problem.type === "RADIO" || problem.type === "MULTI_SELECT") &&
          opts.map((o) => {
            const multi = problem.type === "MULTI_SELECT";
            const checked = value.includes(o.text);
            return (
              <label
                key={o.id}
                className="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-sm hover:bg-slate-50"
              >
                <input
                  type={multi ? "checkbox" : "radio"}
                  name={problem.problemId}
                  checked={checked}
                  disabled={disabled}
                  onChange={() => {
                    if (multi) {
                      onChange(checked ? value.filter((x) => x !== o.text) : [...value, o.text]);
                    } else {
                      onChange([o.text]);
                    }
                  }}
                />
                <span className="text-slate-700">{o.text}</span>
              </label>
            );
          })}
      </div>
    </div>
  );
}

function Report({ report }: { report: AttemptReport }) {
  const pending = report.status === "PENDING_GRADING";
  const graded = report.status === "GRADED";
  return (
    <div className="card">
      {pending ? (
        <>
          <p className="text-sm text-slate-500">Submitted</p>
          <p className="mt-1 text-2xl font-bold text-slate-900">Awaiting teacher grade</p>
          <p className="mt-2 text-sm text-slate-600">
            Your writing is with your teacher. You&apos;ll see the band here once it&apos;s graded.
          </p>
        </>
      ) : (
        <>
          <p className="text-sm text-slate-500">Your result</p>
          {report.band != null ? (
            <p className="mt-1 text-4xl font-bold text-slate-900">Band {report.band.toFixed(1)}</p>
          ) : (
            <p className="mt-1 text-4xl font-bold text-slate-900">
              {report.correct}
              <span className="text-xl text-slate-400"> / {report.total}</span>
            </p>
          )}
          {report.scorePercent != null && (
            <p className="mt-1 text-sm text-slate-500">{report.scorePercent}%</p>
          )}
          {graded && report.criteria && (
            <ul className="mt-4 space-y-1 text-sm text-slate-600">
              {report.criteria.taskAchievement != null && (
                <li className="flex justify-between"><span>Task achievement</span><span>{report.criteria.taskAchievement}</span></li>
              )}
              {report.criteria.coherenceCohesion != null && (
                <li className="flex justify-between"><span>Coherence &amp; cohesion</span><span>{report.criteria.coherenceCohesion}</span></li>
              )}
              {report.criteria.lexicalResource != null && (
                <li className="flex justify-between"><span>Lexical resource</span><span>{report.criteria.lexicalResource}</span></li>
              )}
              {report.criteria.grammaticalRange != null && (
                <li className="flex justify-between"><span>Grammar</span><span>{report.criteria.grammaticalRange}</span></li>
              )}
            </ul>
          )}
          {report.feedback && <p className="mt-4 text-sm text-slate-600">{report.feedback}</p>}
        </>
      )}
      <div className="mt-6 flex gap-2">
        <Link href="/exams" className="btn-ghost">Back to training</Link>
        <Link href="/" className="btn-primary">View progress</Link>
      </div>
    </div>
  );
}
