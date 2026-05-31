"use client";

import { use, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { ApiError, api, type RoadmapDetail } from "@itsh/api-client";
import { RoadmapCanvas } from "@itsh/roadmap";
import { getDone, setDone } from "../../../../lib/roadmapProgress";

export default function RoadmapDetailPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);

  const [roadmap, setRoadmap] = useState<RoadmapDetail | null>(null);
  const [status, setStatus] = useState<"loading" | "ready" | "notfound" | "error">("loading");
  const [done, setDoneState] = useState<Set<string>>(new Set());

  useEffect(() => {
    setDoneState(getDone(slug));
    api
      .getRoadmap(slug)
      .then((r) => {
        setRoadmap(r);
        setStatus("ready");
      })
      .catch((err) => {
        setStatus(err instanceof ApiError && err.status === 404 ? "notfound" : "error");
      });
  }, [slug]);

  function toggle(nodeKey: string) {
    setDoneState((prev) => {
      const next = new Set(prev);
      if (next.has(nodeKey)) next.delete(nodeKey);
      else next.add(nodeKey);
      setDone(slug, next);
      return next;
    });
  }

  function reset() {
    const empty = new Set<string>();
    setDone(slug, empty);
    setDoneState(empty);
  }

  const { total, completed, pct } = useMemo(() => {
    if (!roadmap) return { total: 0, completed: 0, pct: 0 };
    const keys = new Set(roadmap.nodes.map((n) => n.nodeKey));
    const t = roadmap.nodes.length;
    const c = [...done].filter((k) => keys.has(k)).length;
    return { total: t, completed: c, pct: t ? (c / t) * 100 : 0 };
  }, [roadmap, done]);

  if (status === "loading") {
    return <p className="text-sm text-slate-500">Loading roadmap…</p>;
  }

  if (status === "notfound") {
    return (
      <div className="py-12 text-center">
        <p className="text-lg font-semibold text-slate-900">Roadmap not found</p>
        <Link href="/roadmap" className="btn-ghost btn-sm mt-4 inline-flex">
          ← All roadmaps
        </Link>
      </div>
    );
  }

  if (status === "error" || !roadmap) {
    return (
      <div className="py-12 text-center">
        <p className="text-lg font-semibold text-slate-900">Couldn’t load this roadmap</p>
        <p className="mt-1 text-sm text-slate-500">Please try again in a moment.</p>
        <Link href="/roadmap" className="btn-ghost btn-sm mt-4 inline-flex">
          ← All roadmaps
        </Link>
      </div>
    );
  }

  return (
    <>
      <div className="mb-6">
        <Link href="/roadmap" className="text-sm font-medium text-brand-600 hover:text-brand-700">
          ← All roadmaps
        </Link>
      </div>

      <div className="mb-10 overflow-hidden rounded-3xl bg-brand-gradient p-6 text-white shadow-lift sm:p-8">
        <div className="flex items-center gap-4">
          <span className="grid h-14 w-14 shrink-0 place-items-center rounded-2xl bg-white/15 text-3xl">
            {roadmap.icon}
          </span>
          <div>
            <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">{roadmap.title}</h1>
            <p className="text-sm text-white/85">{roadmap.tagline}</p>
          </div>
        </div>
        {roadmap.description && (
          <p className="mt-4 max-w-2xl text-sm text-white/85">{roadmap.description}</p>
        )}
        <div className="mt-5 max-w-md">
          <div className="mb-1 flex items-center justify-between text-xs text-white/80">
            <span>{`${completed} of ${total} complete`}</span>
            <span>{Math.round(pct)}%</span>
          </div>
          <div className="h-2 overflow-hidden rounded-full bg-white/20">
            <div className="h-full rounded-full bg-white transition-all" style={{ width: `${pct}%` }} />
          </div>
        </div>
      </div>

      {/* Legend */}
      <div className="mb-6 flex flex-wrap items-center justify-center gap-4 text-xs text-slate-500">
        <span className="inline-flex items-center gap-1.5">
          <span className="h-3 w-5 rounded-sm border border-slate-900 bg-[#ffdf3d]" /> Core skill
        </span>
        <span className="inline-flex items-center gap-1.5">
          <span className="h-3 w-5 rounded-sm border border-slate-900 bg-[#fbe5a6]" /> Optional
        </span>
        <span className="inline-flex items-center gap-1.5">
          <span className="grid h-4 w-4 place-items-center rounded-full bg-emerald-500 text-[9px] text-white">
            ✓
          </span>
          Tap a node to mark done
        </span>
      </div>

      <RoadmapCanvas nodes={roadmap.nodes} edges={roadmap.edges} done={done} onToggle={toggle} />

      <div className="mx-auto mt-10 flex max-w-3xl items-center justify-between rounded-2xl bg-slate-50 px-5 py-4">
        <p className="text-sm text-slate-500">Progress is saved on this device — no account needed.</p>
        {completed > 0 && (
          <button onClick={reset} className="btn-ghost btn-sm">
            Reset progress
          </button>
        )}
      </div>
    </>
  );
}
