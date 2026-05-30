"use client";

import { useMemo, useState } from "react";
import { api, ApiError, type TypingLesson } from "@itsh/api-client";

export function TypingTest({ lesson }: { lesson: TypingLesson }) {
  const target = lesson.text;
  const [typed, setTyped] = useState("");
  const [startedAt, setStartedAt] = useState<number | null>(null);
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const correct = useMemo(() => {
    let n = 0;
    for (let i = 0; i < typed.length; i++) if (typed[i] === target[i]) n++;
    return n;
  }, [typed, target]);

  function onChange(v: string) {
    if (startedAt === null && v.length > 0) setStartedAt(Date.now());
    setTyped(v.slice(0, target.length));
  }

  async function finish() {
    setError(null);
    const elapsedSec = startedAt ? Math.max(1, (Date.now() - startedAt) / 1000) : 1;
    const accuracy = typed.length ? (correct / typed.length) * 100 : 0;
    const wpm = typed.length / 5 / (elapsedSec / 60);
    try {
      await api.submitTypingSession({
        wpm: Math.round(wpm * 10) / 10,
        accuracyPercent: Math.round(accuracy * 10) / 10,
        durationSeconds: Math.round(elapsedSec),
        keystrokes: typed.length,
        lessonId: lesson.id,
      });
      setResult(`${Math.round(wpm)} WPM · ${Math.round(accuracy)}% accuracy — saved!`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not save session.");
    }
  }

  function reset() {
    setTyped("");
    setStartedAt(null);
    setResult(null);
  }

  return (
    <div>
      <p className="mb-2 select-none rounded-lg bg-slate-100 p-3 font-mono text-sm text-slate-600">
        {target.split("").map((ch, i) => {
          const state =
            i < typed.length ? (typed[i] === ch ? "text-emerald-600" : "bg-red-100 text-red-600") : "";
          return (
            <span key={i} className={state}>
              {ch}
            </span>
          );
        })}
      </p>
      <textarea
        className="input min-h-20 font-mono"
        value={typed}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Start typing…"
        disabled={!!result}
      />
      <div className="mt-2 flex items-center gap-2">
        {!result ? (
          <button className="btn-primary btn-sm" onClick={finish} disabled={typed.length === 0}>
            Finish
          </button>
        ) : (
          <button className="btn-ghost btn-sm" onClick={reset}>
            Try again
          </button>
        )}
        {result && <span className="text-sm font-medium text-emerald-600">{result}</span>}
        {error && <span className="text-sm text-red-600">{error}</span>}
      </div>
    </div>
  );
}
