"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, type RoadmapCard } from "@itsh/api-client";
import { getDone } from "../../../lib/roadmapProgress";
import { PageHeader, ProgressBar } from "../../../components/ui";

export default function RoadmapIndex() {
  const [roadmaps, setRoadmaps] = useState<RoadmapCard[]>([]);
  const [counts, setCounts] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .listRoadmaps({ size: 50 })
      .then((p) => {
        setRoadmaps(p.items);
        const next: Record<string, number> = {};
        for (const r of p.items) next[r.slug] = getDone(r.slug).size;
        setCounts(next);
      })
      .catch(() => setRoadmaps([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <PageHeader
        title="Roadmaps"
        description="Step-by-step paths from zero to job-ready. Follow the order, check off what you learn."
      />
      {loading ? (
        <p className="text-sm text-slate-500">Loading roadmaps…</p>
      ) : roadmaps.length === 0 ? (
        <p className="text-sm text-slate-500">No roadmaps available yet.</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {roadmaps.map((r) => {
            const total = r.nodeCount;
            const done = counts[r.slug] ?? 0;
            const pct = total ? (done / total) * 100 : 0;
            return (
              <Link key={r.slug} href={`/roadmap/${r.slug}`} className="card-hover flex flex-col">
                <div className="flex items-start gap-3">
                  <span className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-brand-50 text-2xl">
                    {r.icon}
                  </span>
                  <div className="min-w-0">
                    <p className="font-semibold text-slate-900">{r.title}</p>
                    <p className="text-sm text-slate-500">{r.tagline}</p>
                  </div>
                </div>
                <div className="mt-4">
                  <div className="mb-1 flex items-center justify-between text-xs text-slate-400">
                    <span>{total} steps</span>
                    <span>{done > 0 ? `${done}/${total} done` : "Not started"}</span>
                  </div>
                  <ProgressBar percent={pct} />
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </>
  );
}
