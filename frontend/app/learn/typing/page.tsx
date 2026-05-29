"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import Nav from "@/components/Nav";
import LearnTabs from "@/components/LearnTabs";
import { api, ApiError, TypingLesson } from "@/lib/api";
import { isAuthenticated } from "@/lib/auth";

export default function TypingPage() {
  const [lessons, setLessons] = useState<TypingLesson[]>([]);
  const [selected, setSelected] = useState<TypingLesson | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .listTypingLessons()
      .then((ls) => {
        setLessons(ls);
        setSelected(ls[0] ?? null);
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load lessons."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-3xl space-y-6 px-4 py-10">
        <LearnTabs />
        <div>
          <h1 className="text-2xl font-bold">Typing practice</h1>
          <p className="mt-1 text-sm text-slate-600">Build speed and accuracy. Stats save when signed in.</p>
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}
        {loading ? (
          <p className="text-slate-500">Loading…</p>
        ) : lessons.length === 0 ? (
          <p className="text-slate-400">No typing lessons available.</p>
        ) : (
          <>
            <div>
              <label className="label">Lesson</label>
              <select
                className="input max-w-sm"
                value={selected?.id ?? ""}
                onChange={(e) => setSelected(lessons.find((l) => l.id === e.target.value) ?? null)}
              >
                {lessons.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.title}
                    {l.difficulty ? ` (${l.difficulty})` : ""}
                  </option>
                ))}
              </select>
            </div>
            {selected && <TypingTest key={selected.id} lesson={selected} />}
          </>
        )}
      </main>
    </>
  );
}

function TypingTest({ lesson }: { lesson: TypingLesson }) {
  const target = lesson.text;
  const [typed, setTyped] = useState("");
  const [startedAt, setStartedAt] = useState<number | null>(null);
  const [finishedAt, setFinishedAt] = useState<number | null>(null);
  const [saveMsg, setSaveMsg] = useState<string | null>(null);
  const saved = useRef(false);

  const correctChars = useMemo(() => {
    let n = 0;
    for (let i = 0; i < typed.length; i++) {
      if (typed[i] === target[i]) n++;
    }
    return n;
  }, [typed, target]);

  const accuracy = typed.length === 0 ? 100 : Math.round((correctChars / typed.length) * 100);
  const elapsedMs = startedAt ? (finishedAt ?? Date.now()) - startedAt : 0;
  const elapsedMin = elapsedMs / 60000;
  const wpm = elapsedMin > 0 ? Math.round(correctChars / 5 / elapsedMin) : 0;

  function onChange(value: string) {
    if (finishedAt) return;
    if (startedAt === null && value.length > 0) setStartedAt(Date.now());
    const next = value.slice(0, target.length);
    setTyped(next);
    if (next.length === target.length) finish(next);
  }

  async function finish(finalTyped: string) {
    const end = Date.now();
    setFinishedAt(end);
    if (!isAuthenticated() || saved.current) return;
    const begin = startedAt ?? end;
    const durationSeconds = Math.max(1, Math.round((end - begin) / 1000));
    let correct = 0;
    for (let i = 0; i < finalTyped.length; i++) if (finalTyped[i] === target[i]) correct++;
    const acc = finalTyped.length === 0 ? 100 : Math.round((correct / finalTyped.length) * 100);
    const finalWpm = Math.round(correct / 5 / (durationSeconds / 60));
    saved.current = true;
    try {
      await api.submitTypingSession({
        wpm: finalWpm,
        accuracyPercent: acc,
        durationSeconds,
        keystrokes: finalTyped.length,
        lessonId: lesson.id,
      });
      setSaveMsg("Session saved.");
    } catch (err) {
      setSaveMsg(err instanceof ApiError ? err.message : "Could not save session.");
    }
  }

  function reset() {
    setTyped("");
    setStartedAt(null);
    setFinishedAt(null);
    setSaveMsg(null);
    saved.current = false;
  }

  return (
    <div className="card space-y-4">
      <div className="flex gap-6 text-sm">
        <Stat label="WPM" value={wpm} />
        <Stat label="Accuracy" value={`${accuracy}%`} />
        <Stat label="Progress" value={`${typed.length}/${target.length}`} />
      </div>

      <p className="rounded-lg bg-slate-50 p-4 font-mono text-sm leading-relaxed">
        {target.split("").map((ch, i) => {
          let cls = "text-slate-400";
          if (i < typed.length) cls = typed[i] === ch ? "text-emerald-600" : "bg-red-100 text-red-600";
          else if (i === typed.length) cls = "bg-brand-100 text-slate-700";
          return (
            <span key={i} className={cls}>
              {ch}
            </span>
          );
        })}
      </p>

      <textarea
        className="input min-h-24 font-mono"
        value={typed}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Start typing…"
        disabled={!!finishedAt}
        autoFocus
      />

      <div className="flex items-center gap-3">
        <button className="btn-ghost" onClick={reset}>
          Reset
        </button>
        {finishedAt && <span className="text-sm font-medium text-emerald-600">Finished!</span>}
        {saveMsg && <span className="text-sm text-slate-500">{saveMsg}</span>}
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <p className="text-lg font-semibold text-slate-900">{value}</p>
    </div>
  );
}
