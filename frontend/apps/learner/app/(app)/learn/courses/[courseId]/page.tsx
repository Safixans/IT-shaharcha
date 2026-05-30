"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import {
  api,
  ApiError,
  type CourseDetail,
  type Enrollment,
} from "@itsh/api-client";
import { ErrorBanner, Loading, PageHeader, ProgressBar } from "../../../../../components/ui";

export default function CoursePage({ params }: { params: Promise<{ courseId: string }> }) {
  const { courseId } = use(params);
  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [done, setDone] = useState<Set<string>>(new Set());
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function refreshEnrollment() {
    const mine = await api.listMyEnrollments().catch(() => []);
    setEnrollment(mine.find((e) => e.courseId === courseId) ?? null);
  }

  useEffect(() => {
    (async () => {
      try {
        setCourse(await api.getCourse(courseId));
        await refreshEnrollment();
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Could not load course.");
      }
    })();
  }, [courseId]);

  async function enroll() {
    setBusy(true);
    setError(null);
    try {
      const e = await api.enroll(courseId);
      setEnrollment(e);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not enroll.");
    } finally {
      setBusy(false);
    }
  }

  async function complete(lessonId: string, moduleId: string, minutes: number | null) {
    setError(null);
    try {
      const p = await api.completeLesson(lessonId, {
        courseId,
        moduleId,
        durationSeconds: (minutes ?? 1) * 60,
        scorePercent: 100,
      });
      setDone((s) => new Set(s).add(lessonId));
      setEnrollment((e) => (e ? { ...e, progressPercent: p.courseProgressPercent } : e));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not record completion.");
    }
  }

  if (course === null && !error) return <Loading />;

  return (
    <>
      <PageHeader
        title={course?.title ?? "Course"}
        description={course ? `${course.level} · ${course.lessonCount} lessons` : undefined}
        action={
          <Link href="/learn" className="btn-ghost">
            ← Catalog
          </Link>
        }
      />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      <div className="card mb-6">
        {enrollment ? (
          <>
            <div className="mb-2 flex items-center justify-between">
              <p className="text-sm font-medium text-slate-700">
                {enrollment.status === "completed" ? "Completed 🎉" : "Your progress"}
              </p>
              <span className="text-sm text-slate-500">{Math.round(enrollment.progressPercent)}%</span>
            </div>
            <ProgressBar percent={enrollment.progressPercent} />
          </>
        ) : (
          <div className="flex items-center justify-between">
            <p className="text-sm text-slate-600">Enroll to track your progress.</p>
            <button className="btn-primary" onClick={enroll} disabled={busy}>
              {busy ? "Enrolling…" : "Enroll"}
            </button>
          </div>
        )}
      </div>

      <div className="space-y-4">
        {course?.modules.length === 0 && (
          <div className="card text-sm text-slate-400">No lessons yet.</div>
        )}
        {course?.modules.map((m) => (
          <div key={m.id} className="card">
            <p className="font-medium text-slate-900">{m.title}</p>
            <ul className="mt-3 divide-y divide-slate-100 border-t border-slate-100">
              {m.lessons.map((l) => {
                const isDone = done.has(l.id);
                return (
                  <li key={l.id} className="flex items-center justify-between py-2 text-sm">
                    <span className="text-slate-700">
                      {l.title}
                      {l.kind && <span className="ml-2 text-xs text-slate-400">{l.kind}</span>}
                    </span>
                    {enrollment ? (
                      isDone ? (
                        <span className="badge bg-emerald-100 text-emerald-700">done</span>
                      ) : (
                        <button className="btn-ghost btn-sm" onClick={() => complete(l.id, m.id, l.estimatedMinutes)}>
                          Mark complete
                        </button>
                      )
                    ) : (
                      <span className="text-xs text-slate-400">enroll to start</span>
                    )}
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </div>
    </>
  );
}
