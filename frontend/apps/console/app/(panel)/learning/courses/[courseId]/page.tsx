"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import {
  api,
  ApiError,
  type CourseDetail,
  type Module,
} from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../../../components/ui";
import { isAdmin } from "@itsh/auth";

const LESSON_KINDS = ["reading", "video", "quiz", "exercise", "typing"];

export default function CourseBuilder({ params }: { params: Promise<{ courseId: string }> }) {
  const { courseId } = use(params);
  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [admin, setAdmin] = useState(false);
  const [moduleTitle, setModuleTitle] = useState("");

  useEffect(() => setAdmin(isAdmin()), []);

  async function load() {
    setError(null);
    try {
      setCourse(await api.getCourse(courseId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load course.");
    }
  }

  useEffect(() => {
    void load();
  }, [courseId]);

  function fail(err: unknown) {
    setError(err instanceof ApiError ? err.message : "Action failed.");
  }

  async function addModule(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createModule({ courseId, title: moduleTitle, order: course?.modules.length ?? 0 });
      setModuleTitle("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  if (course === null && !error) return <Loading />;

  return (
    <>
      <PageHeader
        title={course?.title ?? "Course"}
        description={course ? `${course.level} · ${course.modules.length} module(s)` : undefined}
        action={
          <Link href="/learning" className="btn-ghost">
            ← Back
          </Link>
        }
      />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      <form onSubmit={addModule} className="card mb-6 flex items-end gap-3">
        <div className="flex-1">
          <Field label="Add a module">
            <input className="input" value={moduleTitle} onChange={(e) => setModuleTitle(e.target.value)} placeholder="e.g. Getting started" required />
          </Field>
        </div>
        <button className="btn-primary">Add module</button>
      </form>

      <div className="space-y-4">
        {course?.modules.length === 0 && (
          <div className="card text-sm text-slate-400">No modules yet — add one above.</div>
        )}
        {course?.modules.map((m) => (
          <ModuleCard key={m.id} module={m} admin={admin} onChange={load} onError={fail} />
        ))}
      </div>
    </>
  );
}

function ModuleCard({
  module,
  admin,
  onChange,
  onError,
}: {
  module: Module;
  admin: boolean;
  onChange: () => void;
  onError: (e: unknown) => void;
}) {
  const [open, setOpen] = useState(false);
  const [title, setTitle] = useState("");
  const [kind, setKind] = useState("reading");
  const [minutes, setMinutes] = useState("");
  const [busy, setBusy] = useState(false);

  async function addLesson(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      await api.createLesson({
        moduleId: module.id,
        title,
        order: module.lessons.length,
        kind,
        estimatedMinutes: minutes ? Number(minutes) : undefined,
      });
      setTitle("");
      setMinutes("");
      setOpen(false);
      onChange();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <div>
          <p className="font-medium text-slate-900">{module.title}</p>
          <p className="text-xs text-slate-500">
            {module.lessons.length} lesson{module.lessons.length === 1 ? "" : "s"}
          </p>
        </div>
        <div className="flex gap-2">
          <button className="btn-ghost btn-sm" onClick={() => setOpen((v) => !v)}>
            {open ? "Close" : "Add lesson"}
          </button>
          {admin && (
            <button className="btn-danger btn-sm" onClick={() => api.deleteModule(module.id).then(onChange).catch(onError)}>
              Delete
            </button>
          )}
        </div>
      </div>

      {module.lessons.length > 0 && (
        <ul className="mt-3 divide-y divide-slate-100 border-t border-slate-100">
          {module.lessons.map((l) => (
            <li key={l.id} className="flex items-center justify-between py-2 text-sm">
              <span className="text-slate-700">
                {l.title}
                {l.kind && <span className="ml-2 text-xs text-slate-400">{l.kind}</span>}
              </span>
              {admin && (
                <button className="btn-danger btn-sm" onClick={() => api.deleteLesson(l.id).then(onChange).catch(onError)}>
                  Delete
                </button>
              )}
            </li>
          ))}
        </ul>
      )}

      {open && (
        <form onSubmit={addLesson} className="mt-4 space-y-3 border-t border-slate-100 pt-4">
          <Field label="Lesson title">
            <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field label="Kind">
              <select className="select" value={kind} onChange={(e) => setKind(e.target.value)}>
                {LESSON_KINDS.map((k) => (
                  <option key={k} value={k}>
                    {k}
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Est. minutes">
              <input className="input" type="number" min={0} value={minutes} onChange={(e) => setMinutes(e.target.value)} />
            </Field>
          </div>
          <button className="btn-primary" disabled={busy}>
            {busy ? "Adding…" : "Add lesson"}
          </button>
        </form>
      )}
    </div>
  );
}
