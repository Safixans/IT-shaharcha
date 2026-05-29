"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import Nav from "@/components/Nav";
import LearnTabs from "@/components/LearnTabs";
import { api, ApiError, Course, CourseLevel, Track } from "@/lib/api";

const LEVELS: CourseLevel[] = ["beginner", "intermediate", "advanced"];

export default function LearnPage() {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [trackId, setTrackId] = useState<string>("");
  const [level, setLevel] = useState<CourseLevel | "">("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .listTracks({ size: 100 })
      .then((p) => setTracks(p.items))
      .catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    setError(null);
    api
      .listCourses({ trackId: trackId || undefined, level: level || undefined, size: 100 })
      .then((p) => setCourses(p.items))
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load courses."))
      .finally(() => setLoading(false));
  }, [trackId, level]);

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-5xl space-y-6 px-4 py-10">
        <LearnTabs />

        <div>
          <h1 className="text-2xl font-bold">Course catalog</h1>
          <p className="mt-1 text-sm text-slate-600">Browse tracks and enroll in courses.</p>
        </div>

        <div className="flex flex-wrap items-end gap-3">
          <div>
            <label className="label">Track</label>
            <select className="input" value={trackId} onChange={(e) => setTrackId(e.target.value)}>
              <option value="">All tracks</option>
              {tracks.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.title}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Level</label>
            <select
              className="input"
              value={level}
              onChange={(e) => setLevel(e.target.value as CourseLevel | "")}
            >
              <option value="">All levels</option>
              {LEVELS.map((l) => (
                <option key={l} value={l}>
                  {l}
                </option>
              ))}
            </select>
          </div>
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}
        {loading ? (
          <p className="text-slate-500">Loading…</p>
        ) : courses.length === 0 ? (
          <p className="text-slate-400">No courses match these filters.</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {courses.map((c) => (
              <Link key={c.id} href={`/learn/courses/${c.id}`} className="card hover:border-brand-300">
                <span className="inline-block rounded bg-slate-100 px-2 py-0.5 text-xs capitalize text-slate-600">
                  {c.level}
                </span>
                <h3 className="mt-2 font-semibold text-slate-900">{c.title}</h3>
                {c.summary && <p className="mt-1 text-sm text-slate-600">{c.summary}</p>}
                <p className="mt-3 text-xs text-slate-400">
                  {c.lessonCount} lessons
                  {c.estimatedMinutes ? ` · ~${c.estimatedMinutes} min` : ""}
                </p>
              </Link>
            ))}
          </div>
        )}
      </main>
    </>
  );
}
