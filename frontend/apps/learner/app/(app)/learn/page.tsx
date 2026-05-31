"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, type Course, type Enrollment, type Track } from "@itsh/api-client";
import { Loading, PageHeader, ProgressBar } from "../../../components/ui";

const LEVEL_BADGE: Record<string, string> = {
  beginner: "badge-green",
  intermediate: "badge-amber",
  advanced: "badge-brand",
};

export default function LearnPage() {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [loading, setLoading] = useState(true);
  const [trackFilter, setTrackFilter] = useState<string | null>(null);

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
  const shown = trackFilter ? courses.filter((c) => c.trackId === trackFilter) : courses;

  if (loading) return <Loading />;

  return (
    <>
      <PageHeader title="Learn" description="Browse the catalog and continue where you left off." />

      {active.length > 0 && (
        <section className="mb-10">
          <h2 className="mb-3 text-lg font-semibold text-slate-900">Continue learning</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            {active.map((e) => {
              const c = byId.get(e.courseId);
              return (
                <Link
                  key={e.id}
                  href={`/learn/courses/${e.courseId}`}
                  className="card-hover animate-fade-up"
                >
                  <p className="font-semibold text-slate-900">{c?.title ?? "Course"}</p>
                  <p className="mb-2 mt-1 text-xs text-slate-500">
                    {Math.round(e.progressPercent)}% complete
                  </p>
                  <ProgressBar percent={e.progressPercent} />
                </Link>
              );
            })}
          </div>
        </section>
      )}

      {tracks.length > 0 && (
        <section className="mb-6">
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setTrackFilter(null)}
              className={trackFilter === null ? "badge-brand" : "badge-slate"}
            >
              All tracks
            </button>
            {tracks.map((t) => (
              <button
                key={t.id}
                onClick={() => setTrackFilter(t.id)}
                className={trackFilter === t.id ? "badge-brand" : "badge-slate"}
              >
                {t.title} · {t.courseCount}
              </button>
            ))}
          </div>
        </section>
      )}

      <section>
        <h2 className="mb-3 text-lg font-semibold text-slate-900">
          {trackFilter ? tracks.find((t) => t.id === trackFilter)?.title : "All courses"}
        </h2>
        {shown.length === 0 ? (
          <div className="card text-sm text-slate-400">No courses published yet.</div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {shown.map((c) => (
              <Link key={c.id} href={`/learn/courses/${c.id}`} className="card-hover flex flex-col animate-fade-up">
                <div className="mb-3 flex items-start justify-between gap-2">
                  <span className={LEVEL_BADGE[c.level] ?? "badge-slate"}>{c.level}</span>
                  {c.estimatedMinutes != null && (
                    <span className="text-xs text-slate-400">~{Math.round(c.estimatedMinutes / 60)}h</span>
                  )}
                </div>
                <p className="font-semibold text-slate-900">{c.title}</p>
                {c.summary && <p className="mt-1 line-clamp-3 text-sm text-slate-600">{c.summary}</p>}
                <p className="mt-4 text-xs font-medium text-slate-400">
                  {c.lessonCount} lesson{c.lessonCount === 1 ? "" : "s"}
                </p>
              </Link>
            ))}
          </div>
        )}
      </section>
    </>
  );
}
