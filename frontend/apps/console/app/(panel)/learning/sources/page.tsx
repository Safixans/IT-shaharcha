"use client";

import { useEffect, useState } from "react";
import { api, ApiError, type ContentSource } from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../../components/ui";
import { LearningTabs } from "../../../../components/LearningTabs";
import { isAdmin } from "@itsh/auth";

const STATUS_STYLES: Record<string, string> = {
  ok: "bg-emerald-100 text-emerald-700",
  idle: "bg-slate-100 text-slate-600",
  syncing: "bg-amber-100 text-amber-700",
  error: "bg-red-100 text-red-700",
};

export default function SourcesPage() {
  const [sources, setSources] = useState<ContentSource[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [admin, setAdmin] = useState(false);

  const [name, setName] = useState("");
  const [type, setType] = useState("rss");
  const [target, setTarget] = useState("tutorials");
  const [url, setUrl] = useState("");
  const [topic, setTopic] = useState("");

  useEffect(() => setAdmin(isAdmin()), []);

  async function load() {
    setError(null);
    try {
      setSources(await api.listSources());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load sources.");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  function fail(err: unknown) {
    setError(err instanceof ApiError ? err.message : "Action failed.");
  }

  async function create(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createSource({ name, type, target, url, defaultTopic: topic || undefined });
      setName("");
      setUrl("");
      setTopic("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  async function sync(id: string) {
    setNotice(null);
    try {
      const run = await api.syncSource(id);
      setNotice(`Sync started (${run.status}) — ${run.itemsImported} item(s) so far.`);
      await load();
    } catch (err) {
      fail(err);
    }
  }

  return (
    <>
      <PageHeader title="Content sources" description="External feeds that import tutorials, docs and more." />
      <LearningTabs />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}
      {notice && <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{notice}</div>}

      <div className="grid gap-4 lg:grid-cols-[1fr_18rem]">
        <div>
          {sources === null ? (
            <Loading />
          ) : sources.length === 0 ? (
            <div className="card text-sm text-slate-400">No sources configured.</div>
          ) : (
            <ul className="space-y-3">
              {sources.map((s) => (
                <li key={s.id} className="card">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="font-medium text-slate-900">{s.name}</p>
                        <span className={`badge ${STATUS_STYLES[s.status] ?? "bg-slate-100 text-slate-600"}`}>
                          {s.status}
                        </span>
                      </div>
                      <p className="truncate text-xs text-slate-500">
                        {s.type} → {s.target} · {s.url}
                      </p>
                      <p className="mt-1 text-xs text-slate-400">
                        {s.itemCount} item{s.itemCount === 1 ? "" : "s"}
                        {s.lastSyncedAt ? ` · last synced ${s.lastSyncedAt}` : " · never synced"}
                      </p>
                      {s.lastError && <p className="mt-1 text-xs text-red-600">{s.lastError}</p>}
                    </div>
                    <div className="flex shrink-0 gap-2">
                      <button className="btn-ghost btn-sm" onClick={() => sync(s.id)}>
                        Sync
                      </button>
                      {admin && (
                        <button className="btn-danger btn-sm" onClick={() => api.deleteSource(s.id).then(load).catch(fail)}>
                          Delete
                        </button>
                      )}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <form onSubmit={create} className="card h-fit space-y-3">
          <h3 className="font-semibold text-slate-900">New source</h3>
          <Field label="Name">
            <input className="input" value={name} onChange={(e) => setName(e.target.value)} required />
          </Field>
          <Field label="Type" hint="e.g. rss, youtube, web">
            <input className="input" value={type} onChange={(e) => setType(e.target.value)} required />
          </Field>
          <Field label="Imports into">
            <select className="select" value={target} onChange={(e) => setTarget(e.target.value)}>
              <option value="tutorials">tutorials</option>
              <option value="docs">docs</option>
            </select>
          </Field>
          <Field label="URL">
            <input className="input" value={url} onChange={(e) => setUrl(e.target.value)} required />
          </Field>
          <Field label="Default topic">
            <input className="input" value={topic} onChange={(e) => setTopic(e.target.value)} />
          </Field>
          <button className="btn-primary w-full">Add source</button>
        </form>
      </div>
    </>
  );
}
