"use client";

import { useEffect, useState } from "react";
import {
  api,
  ApiError,
  type Course,
  type CourseLevel,
  type Track,
} from "@itsh/api-client";
import Link from "next/link";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../components/ui";
import { LearningTabs } from "../../../components/LearningTabs";
import { isAdmin } from "@itsh/auth";

const LEVELS: CourseLevel[] = ["beginner", "intermediate", "advanced"];

export default function LearningPage() {
  const [tracks, setTracks] = useState<Track[] | null>(null);
  const [courses, setCourses] = useState<Course[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [admin, setAdmin] = useState(false);

  const [trackTitle, setTrackTitle] = useState("");
  const [trackDesc, setTrackDesc] = useState("");

  const [courseTitle, setCourseTitle] = useState("");
  const [courseLevel, setCourseLevel] = useState<CourseLevel>("beginner");
  const [courseTrack, setCourseTrack] = useState("");

  useEffect(() => setAdmin(isAdmin()), []);

  async function load() {
    setError(null);
    try {
      const [t, c] = await Promise.all([api.listTracks({ size: 100 }), api.listCourses({ size: 100 })]);
      setTracks(t.items);
      setCourses(c.items);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load content.");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  function fail(err: unknown) {
    setError(err instanceof ApiError ? err.message : "Action failed.");
  }

  async function createTrack(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createTrack({ title: trackTitle, description: trackDesc || undefined });
      setTrackTitle("");
      setTrackDesc("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  async function createCourse(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createCourse({
        title: courseTitle,
        level: courseLevel,
        trackId: courseTrack || undefined,
      });
      setCourseTitle("");
      setCourseTrack("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  return (
    <>
      <PageHeader title="Learning content" description="Author tracks and courses." />
      <LearningTabs />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      <div className="space-y-8">
        {/* Tracks */}
        <section>
          <h2 className="mb-3 font-semibold text-slate-900">Tracks</h2>
          <div className="grid gap-4 sm:grid-cols-[1fr_18rem]">
            <div>
              {tracks === null ? (
                <Loading />
              ) : tracks.length === 0 ? (
                <div className="card text-sm text-slate-400">No tracks yet.</div>
              ) : (
                <ul className="overflow-hidden rounded-xl border border-slate-200 bg-white">
                  {tracks.map((t) => (
                    <li key={t.id} className="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
                      <div>
                        <p className="font-medium text-slate-900">{t.title}</p>
                        <p className="text-xs text-slate-500">
                          {t.courseCount} course{t.courseCount === 1 ? "" : "s"} · {t.slug}
                        </p>
                      </div>
                      {admin && (
                        <button className="btn-danger btn-sm" onClick={() => api.deleteTrack(t.id).then(load).catch(fail)}>
                          Delete
                        </button>
                      )}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <form onSubmit={createTrack} className="card h-fit space-y-3">
              <h3 className="font-semibold text-slate-900">New track</h3>
              <Field label="Title">
                <input className="input" value={trackTitle} onChange={(e) => setTrackTitle(e.target.value)} required />
              </Field>
              <Field label="Description">
                <input className="input" value={trackDesc} onChange={(e) => setTrackDesc(e.target.value)} />
              </Field>
              <button className="btn-primary w-full">Create track</button>
            </form>
          </div>
        </section>

        {/* Courses */}
        <section>
          <h2 className="mb-3 font-semibold text-slate-900">Courses</h2>
          <div className="grid gap-4 sm:grid-cols-[1fr_18rem]">
            <div>
              {courses === null ? (
                <Loading />
              ) : courses.length === 0 ? (
                <div className="card text-sm text-slate-400">No courses yet.</div>
              ) : (
                <ul className="overflow-hidden rounded-xl border border-slate-200 bg-white">
                  {courses.map((c) => (
                    <li key={c.id} className="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
                      <div>
                        <p className="font-medium text-slate-900">{c.title}</p>
                        <p className="text-xs text-slate-500">
                          {c.level} · {c.lessonCount} lesson{c.lessonCount === 1 ? "" : "s"}
                        </p>
                      </div>
                      <div className="flex gap-2">
                        <Link href={`/learning/courses/${c.id}`} className="btn-ghost btn-sm">
                          Build
                        </Link>
                        {admin && (
                          <button className="btn-danger btn-sm" onClick={() => api.deleteCourse(c.id).then(load).catch(fail)}>
                            Delete
                          </button>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <form onSubmit={createCourse} className="card h-fit space-y-3">
              <h3 className="font-semibold text-slate-900">New course</h3>
              <Field label="Title">
                <input className="input" value={courseTitle} onChange={(e) => setCourseTitle(e.target.value)} required />
              </Field>
              <Field label="Level">
                <select className="select" value={courseLevel} onChange={(e) => setCourseLevel(e.target.value as CourseLevel)}>
                  {LEVELS.map((l) => (
                    <option key={l} value={l}>
                      {l}
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="Track (optional)">
                <select className="select" value={courseTrack} onChange={(e) => setCourseTrack(e.target.value)}>
                  <option value="">— none —</option>
                  {(tracks ?? []).map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.title}
                    </option>
                  ))}
                </select>
              </Field>
              <button className="btn-primary w-full">Create course</button>
            </form>
          </div>
        </section>
      </div>
    </>
  );
}
