"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, type Course, type Enrollment, type Track } from "@itsh/api-client";
import { Loading, PageHeader, ProgressBar } from "../../../components/ui";

export default function LearnPage() {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      const [t, c, e] = await Promise.all([
        api.listTracks({ size: 100 }).then((p) => p.items).catch(() => []),
        api.listCourses({ size: 100 }).then((p) => p.items).catch(() => []),
        api.listMyEnrollments().catch(() => []),
      ]);
      setTracks(t);
      setCourses(c);
      setEnrollments(e);
      setLoading(false);
    })();
  }, []);

  const byId = new Map(courses.map((c) => [c.id, c]));
  const active = enrollments.filter((e) => e.status === "active");

  if (loading) return <Loading />;

  return (
    <>
      <PageHeader title="Learn" description="Browse the catalog and continue your courses." />

      {active.length > 0 && (
        <section className="mb-8">
          <h2 className="mb-3 text-lg font-semibold text-slate-900">Continue learning</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            {active.map((e) => {
              const c = byId.get(e.courseId);
              return (
                <Link key={e.id} href={`/learn/courses/${e.courseId}`} className="card transition-shadow hover:shadow-md">
                  <p className="font-medium text-slate-900">{c?.title ?? "Course"}</p>
                  <p className="mb-2 mt-1 text-xs text-slate-500">{Math.round(e.progressPercent)}% complete</p>
                  <ProgressBar percent={e.progressPercent} />
                </Link>
              );
            })}
          </div>
        </section>
      )}

      <section>
        <h2 className="mb-3 text-lg font-semibold text-slate-900">All courses</h2>
        {courses.length === 0 ? (
          <div className="card text-sm text-slate-400">No courses published yet.</div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {courses.map((c) => (
              <Link key={c.id} href={`/learn/courses/${c.id}`} className="card transition-shadow hover:shadow-md">
                <p className="font-medium text-slate-900">{c.title}</p>
                {c.summary && <p className="mt-1 line-clamp-2 text-sm text-slate-600">{c.summary}</p>}
                <p className="mt-3 text-xs font-medium text-slate-400">
                  {c.level} · {c.lessonCount} lesson{c.lessonCount === 1 ? "" : "s"}
                </p>
              </Link>
            ))}
          </div>
        )}
      </section>

      {tracks.length > 0 && (
        <section className="mt-8">
          <h2 className="mb-3 text-lg font-semibold text-slate-900">Tracks</h2>
          <div className="flex flex-wrap gap-2">
            {tracks.map((t) => (
              <span key={t.id} className="badge bg-white text-slate-600 ring-1 ring-slate-200">
                {t.title} · {t.courseCount}
              </span>
            ))}
          </div>
        </section>
      )}
    </>
  );
}
