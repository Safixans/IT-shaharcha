"use client";

import { useEffect, useState } from "react";
import AdminGuard from "@/components/AdminGuard";
import { api, ApiError, TypingLesson, TypingLessonInput } from "@/lib/api";

export default function AdminTypingPage() {
  return (
    <AdminGuard>
      <TypingSection />
    </AdminGuard>
  );
}

const EMPTY: TypingLessonInput = { title: "", difficulty: "", text: "" };

function TypingSection() {
  const [items, setItems] = useState<TypingLesson[]>([]);
  const [form, setForm] = useState<TypingLessonInput>(EMPTY);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    load();
  }, []);

  function load() {
    api
      .listTypingLessons()
      .then(setItems)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load lessons."));
  }

  function reset() {
    setForm(EMPTY);
    setEditingId(null);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const payload: TypingLessonInput = {
        title: form.title,
        difficulty: form.difficulty || undefined,
        text: form.text,
      };
      if (editingId) await api.updateTypingLesson(editingId, payload);
      else await api.createTypingLesson(payload);
      reset();
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Save failed.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: string) {
    if (!confirm("Delete this typing lesson?")) return;
    try {
      await api.deleteTypingLesson(id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Delete failed.");
    }
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Typing lessons</h1>
      {error && <p className="text-sm text-red-600">{error}</p>}

      <form onSubmit={submit} className="card space-y-3">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
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
            <label className="label">Difficulty</label>
            <input
              className="input"
              placeholder="easy / medium / hard"
              value={form.difficulty ?? ""}
              onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
            />
          </div>
        </div>
        <div>
          <label className="label">Practice text</label>
          <textarea
            className="input min-h-24 font-mono"
            value={form.text}
            onChange={(e) => setForm({ ...form, text: e.target.value })}
            required
          />
        </div>
        <div className="flex gap-2">
          <button className="btn-primary" disabled={busy}>
            {editingId ? "Update" : "Add lesson"}
          </button>
          {editingId && (
            <button type="button" className="btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <ul className="space-y-2">
        {items.map((l) => (
          <li key={l.id} className="card flex items-center justify-between">
            <div>
              <p className="font-medium">{l.title}</p>
              <p className="text-xs text-slate-400">{l.difficulty ?? "—"}</p>
            </div>
            <div className="flex gap-2">
              <button
                className="btn-ghost px-3 py-1 text-xs"
                onClick={() => {
                  setEditingId(l.id);
                  setForm({ title: l.title, difficulty: l.difficulty ?? "", text: l.text });
                }}
              >
                Edit
              </button>
              <button className="btn-ghost px-3 py-1 text-xs text-red-600" onClick={() => remove(l.id)}>
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
