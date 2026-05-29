"use client";

import { useEffect, useState } from "react";
import AdminGuard from "@/components/AdminGuard";
import { api, ApiError, ContentSource, ContentSourceInput } from "@/lib/api";

export default function AdminSourcesPage() {
  return (
    <AdminGuard>
      <SourcesSection />
    </AdminGuard>
  );
}

const EMPTY: ContentSourceInput = {
  name: "",
  type: "rss",
  target: "tutorial",
  url: "",
  enabled: true,
  schedule: "",
  defaultTopic: "",
};

function SourcesSection() {
  const [items, setItems] = useState<ContentSource[]>([]);
  const [form, setForm] = useState<ContentSourceInput>(EMPTY);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    load();
  }, []);

  function load() {
    api
      .listSources()
      .then(setItems)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load sources."));
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
      const payload: ContentSourceInput = {
        name: form.name,
        type: form.type,
        target: form.target,
        url: form.url,
        enabled: form.enabled,
        schedule: form.schedule || undefined,
        defaultTopic: form.defaultTopic || undefined,
      };
      if (editingId) await api.updateSource(editingId, payload);
      else await api.createSource(payload);
      reset();
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Save failed.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: string) {
    if (!confirm("Delete this source?")) return;
    try {
      await api.deleteSource(id);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Delete failed.");
    }
  }

  async function sync(id: string) {
    setMsg(null);
    try {
      const run = await api.syncSource(id);
      setMsg(`Sync ${run.status} (run ${run.runId.slice(0, 8)})`);
      load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Sync failed.");
    }
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Content sources</h1>
      <p className="text-sm text-slate-600">External feeds that import tutorials and docs.</p>
      {error && <p className="text-sm text-red-600">{error}</p>}
      {msg && <p className="text-sm text-emerald-600">{msg}</p>}

      <form onSubmit={submit} className="card grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <label className="label">Name</label>
          <input
            className="input"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
        </div>
        <div>
          <label className="label">URL</label>
          <input
            className="input"
            value={form.url}
            onChange={(e) => setForm({ ...form, url: e.target.value })}
            required
          />
        </div>
        <div>
          <label className="label">Type</label>
          <select
            className="input"
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          >
            <option value="rss">rss</option>
            <option value="youtube">youtube</option>
            <option value="sitemap">sitemap</option>
          </select>
        </div>
        <div>
          <label className="label">Target</label>
          <select
            className="input"
            value={form.target}
            onChange={(e) => setForm({ ...form, target: e.target.value })}
          >
            <option value="tutorial">tutorial</option>
            <option value="doc">doc</option>
          </select>
        </div>
        <div>
          <label className="label">Schedule (cron)</label>
          <input
            className="input"
            placeholder="0 0 * * *"
            value={form.schedule ?? ""}
            onChange={(e) => setForm({ ...form, schedule: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Default topic</label>
          <input
            className="input"
            value={form.defaultTopic ?? ""}
            onChange={(e) => setForm({ ...form, defaultTopic: e.target.value })}
          />
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-600">
          <input
            type="checkbox"
            checked={form.enabled ?? true}
            onChange={(e) => setForm({ ...form, enabled: e.target.checked })}
          />
          Enabled
        </label>
        <div className="flex items-end gap-2">
          <button className="btn-primary" disabled={busy}>
            {editingId ? "Update" : "Add source"}
          </button>
          {editingId && (
            <button type="button" className="btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <ul className="space-y-2">
        {items.map((s) => (
          <li key={s.id} className="card flex items-center justify-between">
            <div>
              <p className="font-medium">
                {s.name} <span className="text-xs text-slate-400">({s.type} → {s.target})</span>
              </p>
              <p className="text-xs text-slate-400">
                {s.status} · {s.itemCount} items
                {s.lastSyncedAt ? ` · synced ${new Date(s.lastSyncedAt).toLocaleString()}` : ""}
              </p>
              {s.lastError && <p className="text-xs text-red-500">{s.lastError}</p>}
            </div>
            <div className="flex gap-2">
              <button className="btn-ghost px-3 py-1 text-xs" onClick={() => sync(s.id)}>
                Sync
              </button>
              <button
                className="btn-ghost px-3 py-1 text-xs"
                onClick={() => {
                  setEditingId(s.id);
                  setForm({
                    name: s.name,
                    type: s.type,
                    target: s.target,
                    url: s.url,
                    enabled: s.enabled,
                    schedule: s.schedule ?? "",
                    defaultTopic: s.defaultTopic ?? "",
                  });
                }}
              >
                Edit
              </button>
              <button className="btn-ghost px-3 py-1 text-xs text-red-600" onClick={() => remove(s.id)}>
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
