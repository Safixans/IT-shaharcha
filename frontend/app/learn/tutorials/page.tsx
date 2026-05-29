"use client";

import { useEffect, useState } from "react";
import Nav from "@/components/Nav";
import LearnTabs from "@/components/LearnTabs";
import { api, ApiError, Tutorial } from "@/lib/api";
import { isAuthenticated } from "@/lib/auth";

export default function TutorialsPage() {
  const [tutorials, setTutorials] = useState<Tutorial[]>([]);
  const [topic, setTopic] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    api
      .listTutorials({ topic: topic || undefined })
      .then(setTutorials)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load tutorials."))
      .finally(() => setLoading(false));
  }, [topic]);

  async function markWatched(t: Tutorial) {
    if (!isAuthenticated()) return;
    try {
      await api.recordWatched(t.id, {
        watchedSeconds: t.durationSeconds ?? 0,
        positionSeconds: t.durationSeconds ?? 0,
        completed: true,
      });
    } catch {
      // best-effort progress recording
    }
  }

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-5xl space-y-6 px-4 py-10">
        <LearnTabs />
        <div>
          <h1 className="text-2xl font-bold">Video tutorials</h1>
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
        ) : tutorials.length === 0 ? (
          <p className="text-slate-400">No tutorials found.</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {tutorials.map((t) => (
              <div key={t.id} className="card">
                {t.thumbnailUrl && (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={t.thumbnailUrl}
                    alt=""
                    className="mb-3 h-32 w-full rounded-lg object-cover"
                  />
                )}
                <h3 className="font-semibold text-slate-900">{t.title}</h3>
                {t.topic && <p className="text-xs text-slate-400">{t.topic}</p>}
                <a
                  href={t.videoUrl}
                  target="_blank"
                  rel="noreferrer"
                  onClick={() => markWatched(t)}
                  className="btn-primary mt-3 w-full"
                >
                  Watch
                </a>
              </div>
            ))}
          </div>
        )}
      </main>
    </>
  );
}
