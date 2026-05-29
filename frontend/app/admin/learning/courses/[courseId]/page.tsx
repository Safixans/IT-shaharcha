"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import AdminGuard from "@/components/AdminGuard";
import { api, ApiError, CourseDetail, Module } from "@/lib/api";

export default function AdminCoursePage() {
  return (
    <AdminGuard>
      <CourseEditor />
    </AdminGuard>
  );
}

function CourseEditor() {
  const params = useParams<{ courseId: string }>();
  const courseId = params.courseId;
  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [moduleTitle, setModuleTitle] = useState("");

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [courseId]);

  function load() {
    api
      .getCourse(courseId)
      .then(setCourse)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load course."));
  }

  async function addModule(e: React.FormEvent) {
    e.preventDefault();
    if (!moduleTitle.trim()) return;
    try {
      await api.createModule({ courseId, title: moduleTitle });
      setModuleTitle("");
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add module.");
    }
  }

  if (!course) {
    return <p className="text-slate-500">{error ?? "Loading…"}</p>;
  }

  return (
    <div className="space-y-5">
      <div>
        <Link href="/admin/learning" className="text-sm text-brand-600 hover:underline">
          ← Back to courses
        </Link>
        <h1 className="mt-2 text-2xl font-bold">{course.title}</h1>
        <p className="text-sm text-slate-500 capitalize">{course.level}</p>
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}

      {course.modules.map((m) => (
        <ModuleCard key={m.id} module={m} courseId={courseId} onChanged={load} />
      ))}

      <form onSubmit={addModule} className="card flex items-end gap-3">
        <div className="flex-1">
          <label className="label">New module title</label>
          <input
            className="input"
            value={moduleTitle}
            onChange={(e) => setModuleTitle(e.target.value)}
          />
        </div>
        <button className="btn-primary">Add module</button>
      </form>
    </div>
  );
}

function ModuleCard({
  module,
  courseId,
  onChanged,
}: {
  module: Module;
  courseId: string;
  onChanged: () => void;
}) {
  const [lessonTitle, setLessonTitle] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function addLesson(e: React.FormEvent) {
    e.preventDefault();
    if (!lessonTitle.trim()) return;
    try {
      await api.createLesson({ moduleId: module.id, title: lessonTitle });
      setLessonTitle("");
      onChanged();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add lesson.");
    }
  }

  async function removeModule() {
    if (!confirm("Delete this module and its lessons?")) return;
    try {
      await api.deleteModule(module.id);
      onChanged();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to delete module.");
    }
  }

  async function removeLesson(id: string) {
    if (!confirm("Delete this lesson?")) return;
    try {
      await api.deleteLesson(id);
      onChanged();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to delete lesson.");
    }
  }

  return (
    <div className="card space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="font-semibold">{module.title}</h2>
        <button className="btn-ghost px-3 py-1 text-xs text-red-600" onClick={removeModule}>
          Delete module
        </button>
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <ul className="divide-y divide-slate-100">
        {module.lessons.map((l) => (
          <li key={l.id} className="flex items-center justify-between py-2">
            <span className="text-sm text-slate-700">{l.title}</span>
            <button
              className="btn-ghost px-2 py-0.5 text-xs text-red-600"
              onClick={() => removeLesson(l.id)}
            >
              ✕
            </button>
          </li>
        ))}
        {module.lessons.length === 0 && (
          <li className="py-2 text-sm text-slate-400">No lessons yet.</li>
        )}
      </ul>
      <form onSubmit={addLesson} className="flex items-end gap-2">
        <input
          className="input"
          placeholder="New lesson title"
          value={lessonTitle}
          onChange={(e) => setLessonTitle(e.target.value)}
        />
        <button className="btn-ghost">Add</button>
      </form>
    </div>
  );
}
