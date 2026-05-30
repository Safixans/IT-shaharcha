"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, ApiError, type Exam, type ExamType } from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../components/ui";
import { isAdmin } from "@itsh/auth";

const EXAM_TYPES: ExamType[] = ["IELTS", "SAT", "MOCK", "PRACTICE"];

export default function AssessmentsPage() {
  const [exams, setExams] = useState<Exam[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [admin, setAdmin] = useState(false);

  const [title, setTitle] = useState("");
  const [examType, setExamType] = useState<ExamType>("PRACTICE");
  const [duration, setDuration] = useState("");
  const [isReal, setIsReal] = useState(false);

  useEffect(() => setAdmin(isAdmin()), []);

  async function load() {
    setError(null);
    try {
      const page = await api.listExams({ size: 100 });
      setExams(page.items);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load exams.");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function create(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.createExam({
        title,
        examType,
        durationMinutes: duration ? Number(duration) : undefined,
        isRealExam: isReal,
      });
      setTitle("");
      setDuration("");
      setIsReal(false);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create exam.");
    }
  }

  return (
    <>
      <PageHeader title="Assessments" description="Create exams, then open one to build sections & questions." />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      <div className="grid gap-4 sm:grid-cols-[1fr_18rem]">
        <div>
          {exams === null ? (
            <Loading />
          ) : exams.length === 0 ? (
            <div className="card text-sm text-slate-400">No exams yet.</div>
          ) : (
            <ul className="overflow-hidden rounded-xl border border-slate-200 bg-white">
              {exams.map((x) => (
                <li key={x.id} className="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
                  <div>
                    <Link href={`/assessment/${x.id}`} className="font-medium text-brand-600 hover:text-brand-700">
                      {x.title}
                    </Link>
                    <p className="text-xs text-slate-500">
                      {x.examType} · {x.sectionCount} section{x.sectionCount === 1 ? "" : "s"}
                      {x.isRealExam ? " · real exam" : ""}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <Link href={`/assessment/${x.id}`} className="btn-ghost btn-sm">
                      Build
                    </Link>
                    {admin && (
                      <button
                        className="btn-danger btn-sm"
                        onClick={() =>
                          api.deleteExam(x.id).then(load).catch((e) =>
                            setError(e instanceof ApiError ? e.message : "Delete failed."),
                          )
                        }
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <form onSubmit={create} className="card h-fit space-y-3">
          <h3 className="font-semibold text-slate-900">New exam</h3>
          <Field label="Title">
            <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
          </Field>
          <Field label="Type">
            <select className="select" value={examType} onChange={(e) => setExamType(e.target.value as ExamType)}>
              {EXAM_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Duration (minutes)">
            <input className="input" type="number" min={0} value={duration} onChange={(e) => setDuration(e.target.value)} />
          </Field>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input type="checkbox" checked={isReal} onChange={(e) => setIsReal(e.target.checked)} />
            Real (graded) exam
          </label>
          <button className="btn-primary w-full">Create exam</button>
        </form>
      </div>
    </>
  );
}
