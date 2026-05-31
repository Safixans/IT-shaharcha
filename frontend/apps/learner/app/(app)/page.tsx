"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, type ProgressOverview, type RankEntry } from "@itsh/api-client";
import { getAccount } from "@itsh/auth";
import { Loading, PageHeader } from "../../components/ui";

export default function Dashboard() {
  const [name, setName] = useState("");
  const [progress, setProgress] = useState<ProgressOverview | null>(null);
  const [rank, setRank] = useState<RankEntry | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setName(getAccount()?.username ?? "");
    (async () => {
      const [p, r] = await Promise.all([
        api.getProgress().catch(() => null),
        api.getMyRank().then((rs) => rs[0] ?? null).catch(() => null),
      ]);
      setProgress(p);
      setRank(r);
      setLoading(false);
    })();
  }, []);

  return (
    <>
      <div className="mb-8 overflow-hidden rounded-2xl bg-brand-gradient p-6 text-white shadow-lift sm:p-8">
        <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">
          Hi{name ? `, ${name}` : ""} 👋
        </h1>
        <p className="mt-1 max-w-lg text-sm text-white/85">
          Track your progress, keep your streak alive, and climb the rankings across learning, exams, and portfolio.
        </p>
        <div className="mt-5 flex flex-wrap gap-2">
          <Link href="/learn" className="btn bg-white text-brand-700 hover:bg-white/90">
            Continue learning
          </Link>
          <Link href="/rankings" className="btn border border-white/30 text-white hover:bg-white/10">
            View rankings
          </Link>
        </div>
      </div>

      {loading ? (
        <Loading />
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <Stat label="Total points" value={(progress?.totalPoints ?? 0).toLocaleString()} />
            <Stat label="Level" value={String(progress?.overallLevel ?? 0)} />
            <Stat label="Overall rank" value={rank ? `#${rank.rank}` : "—"} />
          </div>

          <h2 className="mb-3 mt-8 text-lg font-semibold text-slate-900">By domain</h2>
          {!progress || progress.domains.length === 0 ? (
            <div className="card text-sm text-slate-400">
              No activity yet — start a course or take an exam to earn points.
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {progress.domains.map((d) => (
                <div key={d.domain} className="card">
                  <div className="flex items-center justify-between">
                    <p className="font-medium capitalize text-slate-900">{d.domain}</p>
                    <span className="badge-brand">{d.points} pts</span>
                  </div>
                  <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500">
                    {Object.entries(d.counters).map(([k, v]) => (
                      <span key={k}>
                        {k.replace(/[._]/g, " ")}: <b className="text-slate-700">{v}</b>
                      </span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="mt-8 grid gap-4 sm:grid-cols-3">
            <Quick href="/learn" title="Keep learning" body="Browse tracks & resume courses" />
            <Quick href="/exams" title="Take an exam" body="Mock IELTS / SAT & practice" />
            <Quick href="/portfolio" title="Build portfolio" body="Add certificates & projects" />
          </div>
        </>
      )}
    </>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="card">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-1 bg-brand-gradient bg-clip-text text-3xl font-bold text-transparent">{value}</p>
    </div>
  );
}

function Quick({ href, title, body }: { href: string; title: string; body: string }) {
  return (
    <Link href={href} className="card-hover">
      <p className="font-semibold text-slate-900">{title}</p>
      <p className="mt-1 text-sm text-slate-600">{body}</p>
    </Link>
  );
}
