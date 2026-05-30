"use client";

import { useEffect, useState } from "react";
import {
  api,
  ApiError,
  type Doc,
  type Tutorial,
  type TypingLesson,
} from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../../components/ui";
import { LearningTabs } from "../../../../components/LearningTabs";
import { isAdmin } from "@itsh/auth";

export default function ContentLibraryPage() {
  const [tutorials, setTutorials] = useState<Tutorial[] | null>(null);
  const [docs, setDocs] = useState<Doc[] | null>(null);
  const [typing, setTyping] = useState<TypingLesson[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [admin, setAdmin] = useState(false);

  // tutorial form
  const [tTitle, setTTitle] = useState("");
  const [tVideo, setTVideo] = useState("");
  const [tTopic, setTTopic] = useState("");
  // doc form
  const [dTitle, setDTitle] = useState("");
  const [dUrl, setDUrl] = useState("");
  const [dTopic, setDTopic] = useState("");
  // typing form
  const [yTitle, setYTitle] = useState("");
  const [yDifficulty, setYDifficulty] = useState("easy");
  const [yText, setYText] = useState("");

  useEffect(() => setAdmin(isAdmin()), []);

  async function load() {
    setError(null);
    try {
      const [t, d, y] = await Promise.all([
        api.listTutorials(),
        api.listDocs(),
        api.listTypingLessons(),
      ]);
      setTutorials(t);
      setDocs(d);
      setTyping(y);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load content.");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  function fail(err: unknown) {
    setError(err instanceof ApiError ? err.message : "Action failed.");
  }

  async function addTutorial(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createTutorial({ title: tTitle, videoUrl: tVideo, topic: tTopic || undefined });
      setTTitle("");
      setTVideo("");
      setTTopic("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  async function addDoc(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createDoc({ title: dTitle, url: dUrl || undefined, topic: dTopic || undefined });
      setDTitle("");
      setDUrl("");
      setDTopic("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  async function addTyping(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createTypingLesson({ title: yTitle, difficulty: yDifficulty, text: yText });
      setYTitle("");
      setYText("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  return (
    <>
      <PageHeader title="Learning content" description="Tutorials, docs, and typing drills." />
      <LearningTabs />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      <div className="space-y-8">
        {/* Tutorials */}
        <Section title="Tutorials">
          <List
            items={tutorials}
            empty="No tutorials yet."
            render={(t) => (
              <Row
                key={t.id}
                title={t.title}
                subtitle={[t.topic, t.videoUrl].filter(Boolean).join(" · ")}
                admin={admin}
                onDelete={() => api.deleteTutorial(t.id).then(load).catch(fail)}
              />
            )}
          />
          <form onSubmit={addTutorial} className="card h-fit space-y-3">
            <h3 className="font-semibold text-slate-900">New tutorial</h3>
            <Field label="Title">
              <input className="input" value={tTitle} onChange={(e) => setTTitle(e.target.value)} required />
            </Field>
            <Field label="Video URL">
              <input className="input" value={tVideo} onChange={(e) => setTVideo(e.target.value)} required />
            </Field>
            <Field label="Topic">
              <input className="input" value={tTopic} onChange={(e) => setTTopic(e.target.value)} />
            </Field>
            <button className="btn-primary w-full">Add tutorial</button>
          </form>
        </Section>

        {/* Docs */}
        <Section title="Docs">
          <List
            items={docs}
            empty="No docs yet."
            render={(d) => (
              <Row
                key={d.id}
                title={d.title}
                subtitle={[d.topic, d.url].filter(Boolean).join(" · ")}
                admin={admin}
                onDelete={() => api.deleteDoc(d.id).then(load).catch(fail)}
              />
            )}
          />
          <form onSubmit={addDoc} className="card h-fit space-y-3">
            <h3 className="font-semibold text-slate-900">New doc</h3>
            <Field label="Title">
              <input className="input" value={dTitle} onChange={(e) => setDTitle(e.target.value)} required />
            </Field>
            <Field label="URL">
              <input className="input" value={dUrl} onChange={(e) => setDUrl(e.target.value)} />
            </Field>
            <Field label="Topic">
              <input className="input" value={dTopic} onChange={(e) => setDTopic(e.target.value)} />
            </Field>
            <button className="btn-primary w-full">Add doc</button>
          </form>
        </Section>

        {/* Typing */}
        <Section title="Typing drills">
          <List
            items={typing}
            empty="No typing lessons yet."
            render={(y) => (
              <Row
                key={y.id}
                title={y.title}
                subtitle={y.difficulty ?? undefined}
                admin={admin}
                onDelete={() => api.deleteTypingLesson(y.id).then(load).catch(fail)}
              />
            )}
          />
          <form onSubmit={addTyping} className="card h-fit space-y-3">
            <h3 className="font-semibold text-slate-900">New typing drill</h3>
            <Field label="Title">
              <input className="input" value={yTitle} onChange={(e) => setYTitle(e.target.value)} required />
            </Field>
            <Field label="Difficulty">
              <select className="select" value={yDifficulty} onChange={(e) => setYDifficulty(e.target.value)}>
                <option value="easy">easy</option>
                <option value="medium">medium</option>
                <option value="hard">hard</option>
              </select>
            </Field>
            <Field label="Text">
              <textarea className="input min-h-24" value={yText} onChange={(e) => setYText(e.target.value)} required />
            </Field>
            <button className="btn-primary w-full">Add drill</button>
          </form>
        </Section>
      </div>
    </>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section>
      <h2 className="mb-3 font-semibold text-slate-900">{title}</h2>
      <div className="grid gap-4 sm:grid-cols-[1fr_18rem]">{children}</div>
    </section>
  );
}

function List<T>({
  items,
  empty,
  render,
}: {
  items: T[] | null;
  empty: string;
  render: (item: T) => React.ReactNode;
}) {
  if (items === null) return <Loading />;
  if (items.length === 0) return <div className="card text-sm text-slate-400">{empty}</div>;
  return (
    <ul className="overflow-hidden rounded-xl border border-slate-200 bg-white">
      {items.map(render)}
    </ul>
  );
}

function Row({
  title,
  subtitle,
  admin,
  onDelete,
}: {
  title: string;
  subtitle?: string;
  admin: boolean;
  onDelete: () => void;
}) {
  return (
    <li className="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
      <div className="min-w-0">
        <p className="truncate font-medium text-slate-900">{title}</p>
        {subtitle && <p className="truncate text-xs text-slate-500">{subtitle}</p>}
      </div>
      {admin && (
        <button className="btn-danger btn-sm shrink-0" onClick={onDelete}>
          Delete
        </button>
      )}
    </li>
  );
}
