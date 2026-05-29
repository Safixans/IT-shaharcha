"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import AdminGuard from "@/components/AdminGuard";
import { api, ApiError, Course, CourseInput, CourseLevel, Track, TrackInput } from "@/lib/api";

const LEVELS: CourseLevel[] = ["beginner", "intermediate", "advanced"];

export default function AdminLearningPage() {
  return (
    <AdminGuard>
      <TracksSection />
      <CoursesSection />
    </AdminGuard>
  );
}

function TracksSection() {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<TrackInput>({ title: "", slug: "", description: "" });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    load();
  }, []);

  function load() {
    api
      .listTracks({ size: 100 })
      .then((p) => setTracks(p.items))
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load tracks."));
  }

  function reset() {
    setForm({ title: "", slug: "", description: "" });
    setEditingId(null);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const payload: TrackInput = {
        title: form.title,
        slug: form.slug || undefined,
        description: form.description || undefined,
      };
      if (editingId) await api.updateTrack(editingId, payload);
      else await api.createTrack(payload);
      reset();
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Save failed.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: string) {
    if (!confirm("Delete this track?")) return;
    try {
      await api.deleteTrack(id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Delete failed.");
    }
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Tracks</h1>
      {error && <p className="text-sm text-red-600">{error}</p>}

      <form onSubmit={submit} className="card grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div>
          <label className="label">Title</label>
          <input
            className="input"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
        </div>
        <div>
          <label className="label">Slug</label>
          <input
            className="input"
            value={form.slug ?? ""}
            onChange={(e) => setForm({ ...form, slug: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Description</label>
          <input
            className="input"
            value={form.description ?? ""}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </div>
        <div className="flex gap-2 sm:col-span-3">
          <button className="btn-primary" disabled={busy}>
            {editingId ? "Update track" : "Add track"}
          </button>
          {editingId && (
            <button type="button" className="btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <ul className="space-y-2">
        {tracks.map((t) => (
          <li key={t.id} className="card flex items-center justify-between">
            <div>
              <p className="font-medium">{t.title}</p>
              <p className="text-xs text-slate-400">
                {t.slug} · {t.courseCount} courses
              </p>
            </div>
            <div className="flex gap-2">
              <button
                className="btn-ghost px-3 py-1 text-xs"
                onClick={() => {
                  setEditingId(t.id);
                  setForm({ title: t.title, slug: t.slug, description: t.description ?? "" });
                }}
              >
                Edit
              </button>
              <button className="btn-ghost px-3 py-1 text-xs text-red-600" onClick={() => remove(t.id)}>
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}

function CoursesSection() {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<CourseInput>({
    trackId: "",
    title: "",
    slug: "",
    summary: "",
    level: "beginner",
    estimatedMinutes: undefined,
  });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.listTracks({ size: 100 }).then((p) => setTracks(p.items)).catch(() => {});
    load();
  }, []);

  function load() {
    api
      .listCourses({ size: 100 })
      .then((p) => setCourses(p.items))
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load courses."));
  }

  function reset() {
    setForm({ trackId: "", title: "", slug: "", summary: "", level: "beginner", estimatedMinutes: undefined });
    setEditingId(null);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const payload: CourseInput = {
        trackId: form.trackId || undefined,
        title: form.title,
        slug: form.slug || undefined,
        summary: form.summary || undefined,
        level: form.level,
        estimatedMinutes: form.estimatedMinutes,
      };
      if (editingId) await api.updateCourse(editingId, payload);
      else await api.createCourse(payload);
      reset();
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Save failed.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: string) {
    if (!confirm("Delete this course?")) return;
    try {
      await api.deleteCourse(id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Delete failed.");
    }
  }

  return (
    <section className="space-y-4 border-t border-slate-200 pt-6">
      <h2 className="text-2xl font-bold">Courses</h2>
      {error && <p className="text-sm text-red-600">{error}</p>}

      <form onSubmit={submit} className="card grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <label className="label">Title</label>
          <input
            className="input"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
        </div>
        <div>
          <label className="label">Track</label>
          <select
            className="input"
            value={form.trackId ?? ""}
            onChange={(e) => setForm({ ...form, trackId: e.target.value })}
          >
            <option value="">— none —</option>
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
            value={form.level}
            onChange={(e) => setForm({ ...form, level: e.target.value as CourseLevel })}
          >
            {LEVELS.map((l) => (
              <option key={l} value={l}>
                {l}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="label">Estimated minutes</label>
          <input
            type="number"
            className="input"
            value={form.estimatedMinutes ?? ""}
            onChange={(e) =>
              setForm({ ...form, estimatedMinutes: e.target.value ? Number(e.target.value) : undefined })
            }
          />
        </div>
        <div className="sm:col-span-2">
          <label className="label">Summary</label>
          <input
            className="input"
            value={form.summary ?? ""}
            onChange={(e) => setForm({ ...form, summary: e.target.value })}
          />
        </div>
        <div className="flex gap-2 sm:col-span-2">
          <button className="btn-primary" disabled={busy}>
            {editingId ? "Update course" : "Add course"}
          </button>
          {editingId && (
            <button type="button" className="btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <ul className="space-y-2">
        {courses.map((c) => (
          <li key={c.id} className="card flex items-center justify-between">
            <div>
              <p className="font-medium">{c.title}</p>
              <p className="text-xs text-slate-400 capitalize">
                {c.level} · {c.lessonCount} lessons
              </p>
            </div>
            <div className="flex gap-2">
              <Link href={`/admin/learning/courses/${c.id}`} className="btn-ghost px-3 py-1 text-xs">
                Modules
              </Link>
              <button
                className="btn-ghost px-3 py-1 text-xs"
                onClick={() => {
                  setEditingId(c.id);
                  setForm({
                    trackId: c.trackId ?? "",
                    title: c.title,
                    slug: c.slug,
                    summary: c.summary ?? "",
                    level: c.level,
                    estimatedMinutes: c.estimatedMinutes ?? undefined,
                  });
                }}
              >
                Edit
              </button>
              <button className="btn-ghost px-3 py-1 text-xs text-red-600" onClick={() => remove(c.id)}>
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
