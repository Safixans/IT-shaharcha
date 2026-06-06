"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { api, ApiError, type AttemptReport, type UnitDetail, type WritingCriteria } from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../../components/ui";

const msg = (e: unknown, fallback: string) => (e instanceof ApiError ? e.message : fallback);

export default function WritingGradingPage() {
  const [queue, setQueue] = useState<AttemptReport[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [openId, setOpenId] = useState<string | null>(null);

  const load = useCallback(() => {
    setQueue(null);
    setError(null);
    api
      .gradingQueue({ size: 100 })
      .then((p) => setQueue(p.items))
      .catch((e) => {
        setQueue([]);
        setError(msg(e, "Could not load the grading queue."));
      });
  }, []);

  useEffect(() => load(), [load]);

  return (
    <>
      <PageHeader
        title="Writing grading"
        description="IELTS Writing submissions from your students, awaiting a band."
        action={
          <Link href="/assessment" className="btn-ghost">
            ← Units
          </Link>
        }
      />
      {error && (
        <div className="mb-4">
          <ErrorBanner message={error} />
        </div>
      )}

      {queue === null ? (
        <Loading />
      ) : queue.length === 0 ? (
        <div className="card text-sm text-slate-400">Nothing pending — you&apos;re all caught up.</div>
      ) : (
        <ul className="space-y-2">
          {queue.map((a) => (
            <GradeRow
              key={a.attemptId}
              attempt={a}
              open={openId === a.attemptId}
              onToggle={() => setOpenId((id) => (id === a.attemptId ? null : a.attemptId))}
              onGraded={load}
              onError={(e) => setError(msg(e, "Could not submit the grade."))}
            />
          ))}
        </ul>
      )}
    </>
  );
}

function GradeRow({
  attempt,
  open,
  onToggle,
  onGraded,
  onError,
}: {
  attempt: AttemptReport;
  open: boolean;
  onToggle: () => void;
  onGraded: () => void;
  onError: (e: unknown) => void;
}) {
  const [unit, setUnit] = useState<UnitDetail | null>(null);

  useEffect(() => {
    if (open && !unit) {
      api.getWriting(attempt.unitId).then(setUnit).catch(() => {});
    }
  }, [open, unit, attempt.unitId]);

  return (
    <li className="card">
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate font-medium text-slate-900">{attempt.title ?? "Writing attempt"}</p>
          <p className="text-xs text-slate-500">
            Submitted {attempt.submittedAt ? new Date(attempt.submittedAt).toLocaleString() : "—"}
          </p>
        </div>
        <button className="btn-ghost btn-sm" onClick={onToggle}>
          {open ? "Close" : "Grade"}
        </button>
      </div>

      {open && (
        <div className="mt-4 space-y-4 border-t border-slate-100 pt-4">
          {unit?.prompt && (
            <div className="rounded-lg border border-slate-100 bg-slate-50 px-3 py-2 text-xs text-slate-600">
              <span className="font-semibold">Prompt:</span> {unit.prompt}
            </div>
          )}
          <div className="whitespace-pre-wrap rounded-lg border border-slate-200 bg-white px-3 py-3 text-sm leading-relaxed text-slate-800">
            {attempt.essay || <span className="text-slate-400">No essay submitted.</span>}
          </div>
          <GradeForm attemptId={attempt.attemptId} onGraded={onGraded} onError={onError} />
        </div>
      )}
    </li>
  );
}

const CRITERIA: { key: keyof WritingCriteria; label: string }[] = [
  { key: "taskAchievement", label: "Task achievement" },
  { key: "coherenceCohesion", label: "Coherence & cohesion" },
  { key: "lexicalResource", label: "Lexical resource" },
  { key: "grammaticalRange", label: "Grammar range & accuracy" },
];

const BANDS = Array.from({ length: 19 }, (_, i) => (i * 0.5).toFixed(1)); // 0.0 .. 9.0

function GradeForm({
  attemptId,
  onGraded,
  onError,
}: {
  attemptId: string;
  onGraded: () => void;
  onError: (e: unknown) => void;
}) {
  const [band, setBand] = useState("6.5");
  const [criteria, setCriteria] = useState<Record<string, string>>({});
  const [feedback, setFeedback] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      const c: WritingCriteria = {};
      for (const { key } of CRITERIA) {
        if (criteria[key]) c[key] = Number(criteria[key]);
      }
      await api.gradeWriting(attemptId, {
        band: Number(band),
        criteria: Object.keys(c).length ? c : undefined,
        feedback: feedback.trim() || undefined,
      });
      onGraded();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        <Field label="Overall band">
          <select className="select" value={band} onChange={(e) => setBand(e.target.value)}>
            {BANDS.map((b) => (
              <option key={b} value={b}>
                {b}
              </option>
            ))}
          </select>
        </Field>
        {CRITERIA.map((c) => (
          <Field key={c.key} label={c.label}>
            <select
              className="select"
              value={criteria[c.key] ?? ""}
              onChange={(e) => setCriteria((s) => ({ ...s, [c.key]: e.target.value }))}
            >
              <option value="">—</option>
              {BANDS.map((b) => (
                <option key={b} value={b}>
                  {b}
                </option>
              ))}
            </select>
          </Field>
        ))}
      </div>
      <Field label="Feedback (optional)">
        <textarea
          className="input min-h-24"
          value={feedback}
          onChange={(e) => setFeedback(e.target.value)}
          placeholder="Strengths, areas to improve…"
        />
      </Field>
      <button className="btn-primary" disabled={busy}>
        {busy ? "Saving…" : "Submit grade"}
      </button>
    </form>
  );
}
