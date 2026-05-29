"use client";

import { useEffect, useState } from "react";
import AdminGuard from "@/components/AdminGuard";
import { api, ApiError, Tutorial, TutorialInput } from "@/lib/api";

export default function AdminTutorialsPage() {
  return (
    <AdminGuard>
      <TutorialsSection />
    </AdminGuard>
  );
}

const EMPTY: TutorialInput = {
  title: "",
  topic: "",
  videoUrl: "",
  durationSeconds: undefined,
  thumbnailUrl: "",
};

function TutorialsSection() {
  const [items, setItems] = useState<Tutorial[]>([]);
  const [form, setForm] = useState<TutorialInput>(EMPTY);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    load();
  }, []);

  function load() {
    api
      .listTutorials()
      .then(setItems)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load tutorials."));
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
      const payload: TutorialInput = {
        title: form.title,
        topic: form.topic || undefined,
        videoUrl: form.videoUrl,
        durationSeconds: form.durationSeconds,
        thumbnailUrl: form.thumbnailUrl || undefined,
      };
      if (editingId) await api.updateTutorial(editingId, payload);
      else await api.createTutorial(payload);
      reset();
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Save failed.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: string) {
    if (!confirm("Delete this tutorial?")) return;
    try {
      await api.deleteTutorial(id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Delete failed.");
    }
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Tutorials</h1>
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
          <label className="label">Topic</label>
          <input
            className="input"
            value={form.topic ?? ""}
            onChange={(e) => setForm({ ...form, topic: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Video URL</label>
          <input
            className="input"
            value={form.videoUrl}
            onChange={(e) => setForm({ ...form, videoUrl: e.target.value })}
            required
          />
        </div>
        <div>
          <label className="label">Thumbnail URL</label>
          <input
            className="input"
            value={form.thumbnailUrl ?? ""}
            onChange={(e) => setForm({ ...form, thumbnailUrl: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Duration (seconds)</label>
          <input
            type="number"
            className="input"
            value={form.durationSeconds ?? ""}
            onChange={(e) =>
              setForm({ ...form, durationSeconds: e.target.value ? Number(e.target.value) : undefined })
            }
          />
        </div>
        <div className="flex items-end gap-2">
          <button className="btn-primary" disabled={busy}>
            {editingId ? "Update" : "Add tutorial"}
          </button>
          {editingId && (
            <button type="button" className="btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <ul className="space-y-2">
        {items.map((t) => (
          <li key={t.id} className="card flex items-center justify-between">
            <div>
              <p className="font-medium">{t.title}</p>
              <p className="text-xs text-slate-400">{t.topic ?? "general"}</p>
            </div>
            <div className="flex gap-2">
              <button
                className="btn-ghost px-3 py-1 text-xs"
                onClick={() => {
                  setEditingId(t.id);
                  setForm({
                    title: t.title,
                    topic: t.topic ?? "",
                    videoUrl: t.videoUrl,
                    durationSeconds: t.durationSeconds ?? undefined,
                    thumbnailUrl: t.thumbnailUrl ?? "",
                  });
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
