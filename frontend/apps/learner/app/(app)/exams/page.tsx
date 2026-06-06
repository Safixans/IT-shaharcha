"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { api, type AttemptFamily, type UnitMeta } from "@itsh/api-client";
import { Loading, PageHeader } from "../../../components/ui";

type FamilyKey = "listening" | "reading" | "writing" | "sat" | "quiz";

const TABS: { key: FamilyKey; label: string; family: AttemptFamily }[] = [
  { key: "listening", label: "Listening", family: "IELTS_LISTENING" },
  { key: "reading", label: "Reading", family: "IELTS_READING" },
  { key: "writing", label: "Writing", family: "IELTS_WRITING" },
  { key: "sat", label: "SAT", family: "SAT" },
  { key: "quiz", label: "Quizzes", family: "QUIZ" },
];

function listFor(key: FamilyKey): Promise<UnitMeta[]> {
  const q = { size: 100 };
  const map: Record<FamilyKey, () => Promise<{ items: UnitMeta[] }>> = {
    listening: () => api.listListening(q),
    reading: () => api.listReading(q),
    writing: () => api.listWriting(q),
    sat: () => api.listSatModules(q),
    quiz: () => api.listQuizzes(q),
  };
  return map[key]().then((p) => p.items);
}

export default function ExamsPage() {
  const [tab, setTab] = useState<FamilyKey>("listening");
  const [units, setUnits] = useState<UnitMeta[] | null>(null);

  const load = useCallback(() => {
    setUnits(null);
    listFor(tab)
      .then(setUnits)
      .catch(() => setUnits([]));
  }, [tab]);

  useEffect(() => load(), [load]);

  return (
    <>
      <PageHeader title="Training" description="Single-skill IELTS, SAT modules, and quizzes — train, don't cram." />

      <div className="mb-6 flex flex-wrap gap-1 border-b border-slate-200">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium ${
              tab === t.key
                ? "border-brand-500 text-brand-700"
                : "border-transparent text-slate-500 hover:text-slate-700"
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {units === null ? (
        <Loading />
      ) : units.length === 0 ? (
        <div className="card text-sm text-slate-400">No units available yet.</div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {units.map((u) => (
            <Link
              key={u.id}
              href={`/exams/${u.id}?family=${u.family}`}
              className="card transition-shadow hover:shadow-md"
            >
              <p className="font-medium text-slate-900">{u.title}</p>
              <p className="mt-2 text-xs text-slate-500">
                {u.problemCount > 0 && `${u.problemCount} question${u.problemCount === 1 ? "" : "s"}`}
                {u.satSection ? ` · ${u.satSection === "MATH" ? "Math" : "R&W"}` : ""}
                {u.writingTask ? ` · ${u.writingTask === "TASK_1" ? "Task 1" : "Task 2"}` : ""}
                {u.durationSeconds ? ` · ${Math.round(u.durationSeconds / 60)} min` : ""}
              </p>
              {u.tags?.length > 0 && (
                <div className="mt-2 flex flex-wrap gap-1">
                  {u.tags.slice(0, 3).map((t) => (
                    <span key={t} className="badge bg-slate-100 text-slate-500">
                      {t}
                    </span>
                  ))}
                </div>
              )}
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
