"use client";

import { useEffect, useState } from "react";
import Nav from "@/components/Nav";
import LearnTabs from "@/components/LearnTabs";
import { api, ApiError, Doc } from "@/lib/api";
import { isAuthenticated } from "@/lib/auth";

export default function DocsPage() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const [topic, setTopic] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    api
      .listDocs({ topic: topic || undefined })
      .then(setDocs)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load docs."))
      .finally(() => setLoading(false));
  }, [topic]);

  async function markRead(d: Doc) {
    if (!isAuthenticated()) return;
    try {
      await api.recordRead(d.id, { scrollPercent: 100 });
    } catch {
      // best-effort
    }
  }

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-5xl space-y-6 px-4 py-10">
        <LearnTabs />
        <div>
          <h1 className="text-2xl font-bold">Documentation & articles</h1>
          <input
            className="input mt-3 max-w-xs"
            placeholder="Filter by topic…"
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
          />
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}
        {loading ? (
          <p className="text-slate-500">Loading…</p>
        ) : docs.length === 0 ? (
          <p className="text-slate-400">No docs found.</p>
        ) : (
          <ul className="space-y-3">
            {docs.map((d) => (
              <li key={d.id} className="card flex items-center justify-between">
                <div>
                  <a
                    href={d.url}
                    target="_blank"
                    rel="noreferrer"
                    onClick={() => markRead(d)}
                    className="font-medium text-brand-600 hover:underline"
                  >
                    {d.title}
                  </a>
                  <p className="text-xs text-slate-400">
                    {d.topic ?? "general"}
                    {d.estimatedMinutes ? ` · ~${d.estimatedMinutes} min read` : ""}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        )}
      </main>
    </>
  );
}
