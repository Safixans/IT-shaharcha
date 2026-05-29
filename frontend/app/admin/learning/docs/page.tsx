"use client";

import { useEffect, useState } from "react";
import AdminGuard from "@/components/AdminGuard";
import { api, ApiError, Doc, DocInput } from "@/lib/api";

export default function AdminDocsPage() {
  return (
    <AdminGuard>
      <DocsSection />
    </AdminGuard>
  );
}

const EMPTY: DocInput = { title: "", topic: "", url: "", estimatedMinutes: undefined };

function DocsSection() {
  const [items, setItems] = useState<Doc[]>([]);
  const [form, setForm] = useState<DocInput>(EMPTY);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    load();
  }, []);

  function load() {
    api
      .listDocs()
      .then(setItems)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load docs."));
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
      const payload: DocInput = {
        title: form.title,
        topic: form.topic || undefined,
        url: form.url || undefined,
        estimatedMinutes: form.estimatedMinutes,
      };
      if (editingId) await api.updateDoc(editingId, payload);
      else await api.createDoc(payload);
      reset();
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Save failed.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: string) {
    if (!confirm("Delete this doc?")) return;
    try {
      await api.deleteDoc(id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Delete failed.");
    }
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Docs & articles</h1>
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
          <label className="label">URL</label>
          <input
            className="input"
            value={form.url ?? ""}
            onChange={(e) => setForm({ ...form, url: e.target.value })}
          />
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
        <div className="flex items-end gap-2 sm:col-span-2">
          <button className="btn-primary" disabled={busy}>
            {editingId ? "Update" : "Add doc"}
          </button>
          {editingId && (
            <button type="button" className="btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <ul className="space-y-2">
        {items.map((d) => (
          <li key={d.id} className="card flex items-center justify-between">
            <div>
              <p className="font-medium">{d.title}</p>
              <p className="text-xs text-slate-400">{d.topic ?? "general"}</p>
            </div>
            <div className="flex gap-2">
              <button
                className="btn-ghost px-3 py-1 text-xs"
                onClick={() => {
                  setEditingId(d.id);
                  setForm({
                    title: d.title,
                    topic: d.topic ?? "",
                    url: d.url,
                    estimatedMinutes: d.estimatedMinutes ?? undefined,
                  });
                }}
              >
                Edit
              </button>
              <button className="btn-ghost px-3 py-1 text-xs text-red-600" onClick={() => remove(d.id)}>
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
