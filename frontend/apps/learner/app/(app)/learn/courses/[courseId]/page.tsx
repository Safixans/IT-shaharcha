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
      setEnrollment(await api.enroll(courseId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not enroll.");
    } finally {
      setBusy(false);
    }
  }

  const firstLessonId = course?.modules.flatMap((m) => m.lessons)[0]?.id;

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
              <span className="text-sm font-semibold text-slate-500">
                {Math.round(enrollment.progressPercent)}%
              </span>
            </div>
            <ProgressBar percent={enrollment.progressPercent} />
            {firstLessonId && (
              <Link
                href={`/learn/courses/${courseId}/lessons/${firstLessonId}`}
                className="btn-primary mt-4"
              >
                {enrollment.progressPercent > 0 ? "Continue learning" : "Start first lesson"}
              </Link>
            )}
          </>
        ) : (
          <div className="flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-slate-600">Enroll to track your progress through this course.</p>
            <div className="flex gap-2">
              {firstLessonId && (
                <Link href={`/learn/courses/${courseId}/lessons/${firstLessonId}`} className="btn-ghost">
                  Preview
                </Link>
              )}
              <button className="btn-primary" onClick={enroll} disabled={busy}>
                {busy ? "Enrolling…" : "Enroll"}
              </button>
            </div>
          </div>
        )}
      </div>

      <div className="space-y-4">
        {course?.modules.length === 0 && (
          <div className="card text-sm text-slate-400">No lessons yet.</div>
        )}
        {course?.modules.map((m, mi) => (
          <div key={m.id} className="card">
            <div className="mb-3 flex items-center gap-2">
              <span className="grid h-6 w-6 place-items-center rounded-md bg-brand-50 text-xs font-bold text-brand-700">
                {mi + 1}
              </span>
              <p className="font-semibold text-slate-900">{m.title}</p>
            </div>
            <ul className="divide-y divide-slate-100 border-t border-slate-100">
              {m.lessons.map((l) => (
                <li key={l.id}>
                  <Link
                    href={`/learn/courses/${courseId}/lessons/${l.id}`}
                    className="group flex items-center justify-between gap-3 py-2.5 text-sm"
                  >
                    <span className="flex items-center gap-2 text-slate-700 group-hover:text-brand-700">
                      {l.title}
                      {l.kind && <span className="badge-slate">{l.kind}</span>}
                    </span>
                    <span className="flex items-center gap-3 text-xs text-slate-400">
                      {l.estimatedMinutes != null && <span>{l.estimatedMinutes} min</span>}
                      <span className="text-slate-300 group-hover:text-brand-500">→</span>
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </>
  );
}
