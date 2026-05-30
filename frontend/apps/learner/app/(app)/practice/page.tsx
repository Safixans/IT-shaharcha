"use client";

import { useEffect, useState } from "react";
import { api, type Doc, type Tutorial, type TypingLesson } from "@itsh/api-client";
import { Loading, PageHeader } from "../../../components/ui";
import { TypingTest } from "../../../components/TypingTest";

export default function PracticePage() {
  const [tutorials, setTutorials] = useState<Tutorial[]>([]);
  const [docs, setDocs] = useState<Doc[]>([]);
  const [typing, setTyping] = useState<TypingLesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [marked, setMarked] = useState<Set<string>>(new Set());
  const [activeTyping, setActiveTyping] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      const [t, d, y] = await Promise.all([
        api.listTutorials().catch(() => []),
        api.listDocs().catch(() => []),
        api.listTypingLessons().catch(() => []),
      ]);
      setTutorials(t);
      setDocs(d);
      setTyping(y);
      setLoading(false);
    })();
  }, []);

  function mark(id: string) {
    setMarked((s) => new Set(s).add(id));
  }

  async function watch(t: Tutorial) {
    await api.recordWatched(t.id, { watchedSeconds: t.durationSeconds ?? 60, completed: true }).catch(() => {});
    mark(t.id);
  }

  async function read(d: Doc) {
    await api.recordRead(d.id, { durationSeconds: (d.estimatedMinutes ?? 1) * 60 }).catch(() => {});
    mark(d.id);
  }

  if (loading) return <Loading />;

  return (
    <>
      <PageHeader title="Practice" description="Watch tutorials, read docs, and drill your typing." />

      <section className="mb-8">
        <h2 className="mb-3 text-lg font-semibold text-slate-900">Tutorials</h2>
        {tutorials.length === 0 ? (
          <div className="card text-sm text-slate-400">No tutorials yet.</div>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2">
            {tutorials.map((t) => (
              <div key={t.id} className="card flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <a href={t.videoUrl} target="_blank" rel="noopener noreferrer" className="font-medium text-brand-600 hover:text-brand-700">
                    {t.title}
                  </a>
                  {t.topic && <p className="text-xs text-slate-500">{t.topic}</p>}
                </div>
                {marked.has(t.id) ? (
                  <span className="badge bg-emerald-100 text-emerald-700">watched</span>
                ) : (
                  <button className="btn-ghost btn-sm shrink-0" onClick={() => watch(t)}>
                    Mark watched
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="mb-8">
        <h2 className="mb-3 text-lg font-semibold text-slate-900">Docs</h2>
        {docs.length === 0 ? (
          <div className="card text-sm text-slate-400">No docs yet.</div>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2">
            {docs.map((d) => (
              <div key={d.id} className="card flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <a href={d.url} target="_blank" rel="noopener noreferrer" className="font-medium text-brand-600 hover:text-brand-700">
                    {d.title}
                  </a>
                  {d.topic && <p className="text-xs text-slate-500">{d.topic}</p>}
                </div>
                {marked.has(d.id) ? (
                  <span className="badge bg-emerald-100 text-emerald-700">read</span>
                ) : (
                  <button className="btn-ghost btn-sm shrink-0" onClick={() => read(d)}>
                    Mark read
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="mb-3 text-lg font-semibold text-slate-900">Typing drills</h2>
        {typing.length === 0 ? (
          <div className="card text-sm text-slate-400">No typing drills yet.</div>
        ) : (
          <div className="space-y-3">
            {typing.map((y) => (
              <div key={y.id} className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-slate-900">{y.title}</p>
                    {y.difficulty && <p className="text-xs text-slate-500">{y.difficulty}</p>}
                  </div>
                  <button
                    className="btn-ghost btn-sm"
                    onClick={() => setActiveTyping((id) => (id === y.id ? null : y.id))}
                  >
                    {activeTyping === y.id ? "Close" : "Start"}
                  </button>
                </div>
                {activeTyping === y.id && (
                  <div className="mt-4 border-t border-slate-100 pt-4">
                    <TypingTest lesson={y} />
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </section>
    </>
  );
}
