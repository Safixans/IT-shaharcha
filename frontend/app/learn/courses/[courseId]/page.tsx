"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Nav from "@/components/Nav";
import { api, ApiError, CourseDetail, Enrollment, Lesson } from "@/lib/api";
import { isAuthenticated } from "@/lib/auth";

export default function CourseDetailPage() {
  const params = useParams<{ courseId: string }>();
  const router = useRouter();
  const courseId = params.courseId;

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [courseId]);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const c = await api.getCourse(courseId);
      setCourse(c);
      if (isAuthenticated()) {
        const mine = await api.listMyEnrollments();
        setEnrollment(mine.find((e) => e.courseId === courseId) ?? null);
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load course.");
    } finally {
      setLoading(false);
    }
  }

  async function enroll() {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      setEnrollment(await api.enroll(courseId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to enroll.");
    } finally {
      setBusy(false);
    }
  }

  async function onLessonCompleted(progress: number) {
    setEnrollment((prev) =>
      prev
        ? {
            ...prev,
            progressPercent: progress,
            status: progress >= 100 ? "completed" : prev.status,
          }
        : prev,
    );
  }

  if (loading) {
    return (
      <>
        <Nav />
        <main className="mx-auto max-w-3xl px-4 py-16 text-slate-500">Loading…</main>
      </>
    );
  }

  if (!course) {
    return (
      <>
        <Nav />
        <main className="mx-auto max-w-3xl px-4 py-16">
          <p className="text-red-600">{error ?? "Course not found."}</p>
        </main>
      </>
    );
  }

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-3xl space-y-6 px-4 py-10">
        <div>
          <span className="inline-block rounded bg-slate-100 px-2 py-0.5 text-xs capitalize text-slate-600">
            {course.level}
          </span>
          <h1 className="mt-2 text-2xl font-bold">{course.title}</h1>
          {course.summary && <p className="mt-1 text-slate-600">{course.summary}</p>}
          <p className="mt-2 text-sm text-slate-400">
            {course.lessonCount} lessons
            {course.estimatedMinutes ? ` · ~${course.estimatedMinutes} min` : ""}
          </p>
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="card flex items-center justify-between">
          {enrollment ? (
            <div className="w-full">
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium capitalize">{enrollment.status}</span>
                <span className="text-slate-500">{enrollment.progressPercent}%</span>
              </div>
              <div className="mt-2 h-2 w-full rounded-full bg-slate-100">
                <div
                  className="h-2 rounded-full bg-brand-500 transition-all"
                  style={{ width: `${enrollment.progressPercent}%` }}
                />
              </div>
            </div>
          ) : (
            <>
              <p className="text-sm text-slate-600">Enroll to track your progress.</p>
              <button className="btn-primary" onClick={enroll} disabled={busy}>
                {busy ? "Enrolling…" : "Enroll"}
              </button>
            </>
          )}
        </div>

        <div className="space-y-4">
          {course.modules.map((m) => (
            <div key={m.id} className="card">
              <h2 className="font-semibold text-slate-900">{m.title}</h2>
              <ul className="mt-3 divide-y divide-slate-100">
                {m.lessons.map((lesson) => (
                  <LessonRow
                    key={lesson.id}
                    lesson={lesson}
                    courseId={courseId}
                    moduleId={m.id}
                    enrolled={!!enrollment}
                    onCompleted={onLessonCompleted}
                  />
                ))}
              </ul>
            </div>
          ))}
          {course.modules.length === 0 && (
            <p className="text-slate-400">This course has no modules yet.</p>
          )}
        </div>
      </main>
    </>
  );
}

function LessonRow({
  lesson,
  courseId,
  moduleId,
  enrolled,
  onCompleted,
}: {
  lesson: Lesson;
  courseId: string;
  moduleId: string;
  enrolled: boolean;
  onCompleted: (courseProgress: number) => void;
}) {
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  async function start() {
    setBusy(true);
    setMsg(null);
    try {
      await api.startLesson(lesson.id);
      setMsg("Started");
    } catch (err) {
      setMsg(err instanceof ApiError ? err.message : "Failed");
    } finally {
      setBusy(false);
    }
  }

  async function complete() {
    setBusy(true);
    setMsg(null);
    try {
      const p = await api.completeLesson(lesson.id, {
        courseId,
        moduleId,
        durationSeconds: (lesson.estimatedMinutes ?? 1) * 60,
        scorePercent: 100,
      });
      setDone(true);
      onCompleted(p.courseProgressPercent);
    } catch (err) {
      setMsg(err instanceof ApiError ? err.message : "Failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <li className="flex items-center justify-between py-2">
      <div>
        <p className="text-sm font-medium text-slate-800">{lesson.title}</p>
        <p className="text-xs text-slate-400">
          {lesson.kind ?? "lesson"}
          {lesson.estimatedMinutes ? ` · ~${lesson.estimatedMinutes} min` : ""}
        </p>
      </div>
      {enrolled && (
        <div className="flex items-center gap-2">
          {msg && <span className="text-xs text-slate-400">{msg}</span>}
          <button className="btn-ghost px-3 py-1 text-xs" onClick={start} disabled={busy}>
            Start
          </button>
          <button
            className="btn-primary px-3 py-1 text-xs"
            onClick={complete}
            disabled={busy || done}
          >
            {done ? "Done" : "Complete"}
          </button>
        </div>
      )}
    </li>
  );
}
