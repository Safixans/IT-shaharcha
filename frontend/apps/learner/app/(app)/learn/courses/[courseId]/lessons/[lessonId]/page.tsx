"use client";

import { use, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import {
  api,
  ApiError,
  type CourseDetail,
  type Enrollment,
  type LessonDetail,
} from "@itsh/api-client";
import { ErrorBanner, Loading } from "../../../../../../../components/ui";
import { Markdown } from "../../../../../../../components/Markdown";

type FlatLesson = { id: string; title: string; moduleId: string; estimatedMinutes: number | null };

export default function LessonPage({
  params,
}: {
  params: Promise<{ courseId: string; lessonId: string }>;
}) {
  const { courseId, lessonId } = use(params);
  const [lesson, setLesson] = useState<LessonDetail | null>(null);
  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [completed, setCompleted] = useState(false);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const [l, c, mine] = await Promise.all([
          api.getLesson(lessonId),
          api.getCourse(courseId),
          api.listMyEnrollments().catch(() => []),
        ]);
        setLesson(l);
        setCourse(c);
        setEnrollment(mine.find((e) => e.courseId === courseId) ?? null);
        setCompleted(false);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Could not load lesson.");
      }
    })();
  }, [courseId, lessonId]);

  const flat: FlatLesson[] = useMemo(
    () =>
      (course?.modules ?? []).flatMap((m) =>
        m.lessons.map((l) => ({
          id: l.id,
          title: l.title,
          moduleId: m.id,
          estimatedMinutes: l.estimatedMinutes,
        })),
      ),
    [course],
  );

  const index = flat.findIndex((l) => l.id === lessonId);
  const prev = index > 0 ? flat[index - 1] : null;
  const next = index >= 0 && index < flat.length - 1 ? flat[index + 1] : null;
  const current = flat[index];

  async function markComplete() {
    if (!current) return;
    setBusy(true);
    setError(null);
    try {
      await api.completeLesson(lessonId, {
        courseId,
        moduleId: current.moduleId,
        durationSeconds: (current.estimatedMinutes ?? 1) * 60,
        scorePercent: 100,
      });
      setCompleted(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not record completion.");
    } finally {
      setBusy(false);
    }
  }

  if (!lesson && !error) return <Loading />;

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_260px]">
      <article className="animate-fade-up">
        <nav className="mb-4 flex items-center gap-2 text-xs text-slate-500">
          <Link href="/learn" className="hover:text-brand-600">
            Catalog
          </Link>
          <span>/</span>
          <Link href={`/learn/courses/${courseId}`} className="hover:text-brand-600">
            {course?.title ?? "Course"}
          </Link>
        </nav>

        {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

        <div className="card">
          {lesson?.estimatedMinutes != null && (
            <span className="badge-brand mb-4">{lesson.estimatedMinutes} min read</span>
          )}
          {lesson?.body ? (
            <Markdown source={lesson.body} />
          ) : (
            <p className="text-sm text-slate-400">This lesson has no written content yet.</p>
          )}
        </div>

        <div className="mt-6 flex items-center justify-between gap-3">
          {prev ? (
            <Link href={`/learn/courses/${courseId}/lessons/${prev.id}`} className="btn-ghost">
              ← Previous
            </Link>
          ) : (
            <span />
          )}

          {enrollment ? (
            completed ? (
              <span className="badge-green">Completed ✓</span>
            ) : (
              <button className="btn-primary" onClick={markComplete} disabled={busy}>
                {busy ? "Saving…" : "Mark complete"}
              </button>
            )
          ) : (
            <Link href={`/learn/courses/${courseId}`} className="btn-primary">
              Enroll to track progress
            </Link>
          )}

          {next ? (
            <Link href={`/learn/courses/${courseId}/lessons/${next.id}`} className="btn-primary">
              Next →
            </Link>
          ) : (
            <Link href={`/learn/courses/${courseId}`} className="btn-ghost">
              Back to course
            </Link>
          )}
        </div>
      </article>

      <aside className="hidden lg:block">
        <div className="sticky top-6">
          <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-400">
            In this course
          </p>
          <nav className="space-y-4">
            {course?.modules.map((m) => (
              <div key={m.id}>
                <p className="mb-1 text-xs font-semibold text-slate-500">{m.title}</p>
                <ul className="space-y-0.5">
                  {m.lessons.map((l) => (
                    <li key={l.id}>
                      <Link
                        href={`/learn/courses/${courseId}/lessons/${l.id}`}
                        className={`block rounded-md px-2 py-1 text-sm ${
                          l.id === lessonId
                            ? "bg-brand-50 font-medium text-brand-700"
                            : "text-slate-600 hover:bg-slate-100"
                        }`}
                      >
                        {l.title}
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </nav>
        </div>
      </aside>
    </div>
  );
}
