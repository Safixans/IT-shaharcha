"use client";

import { useEffect, useState } from "react";
import { api, type Leaderboard, type RankEntry, type ServiceDomain } from "@itsh/api-client";
import { getAccount } from "@itsh/auth";
import { Loading, PageHeader } from "../../../components/ui";

const DOMAINS: (ServiceDomain | "overall")[] = ["overall", "learning", "assessment", "portfolio"];

export default function RankingsPage() {
  const [domain, setDomain] = useState<ServiceDomain | "overall">("overall");
  const [board, setBoard] = useState<Leaderboard | null>(null);
  const [mine, setMine] = useState<RankEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const me = typeof window !== "undefined" ? getAccount()?.id : undefined;

  useEffect(() => {
    setLoading(true);
    const d = domain === "overall" ? undefined : domain;
    Promise.all([
      api.getLeaderboard({ domain: d, size: 50 }).catch(() => null),
      api.getMyRank().catch(() => []),
    ]).then(([b, r]) => {
      setBoard(b);
      setMine(r);
      setLoading(false);
    });
  }, [domain]);

  const myEntry = mine.find((r) => (domain === "overall" ? r.domain == null : r.domain === domain));

  return (
    <>
      <PageHeader title="Rankings" description="See how you stack up across the platform." />

      <div className="mb-4 flex flex-wrap gap-2">
        {DOMAINS.map((d) => (
          <button
            key={d}
            onClick={() => setDomain(d)}
            className={`rounded-full px-3 py-1.5 text-sm font-medium capitalize ${
              domain === d ? "bg-brand-500 text-white" : "bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-100"
            }`}
          >
            {d}
          </button>
        ))}
      </div>

      {myEntry && (
        <div className="card mb-4 flex items-center justify-between bg-brand-50">
          <span className="text-sm font-medium text-brand-700">Your rank</span>
          <span className="text-sm text-brand-700">
            #{myEntry.rank} · {myEntry.points.toLocaleString()} pts
            {myEntry.level != null && ` · Lv ${myEntry.level}`}
          </span>
        </div>
      )}

      {loading ? (
        <Loading />
      ) : !board || board.entries.length === 0 ? (
        <div className="card text-sm text-slate-400">No ranked accounts yet.</div>
      ) : (
        <ol className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          {board.entries.map((e) => {
            const isMe = me && e.accountId === me;
            return (
              <li
                key={`${e.accountId}-${e.rank}`}
                className={`flex items-center gap-4 border-b border-slate-100 px-4 py-3 last:border-0 ${
                  isMe ? "bg-brand-50" : ""
                }`}
              >
                <span className="w-8 shrink-0 text-center text-sm font-bold text-slate-400">{e.rank}</span>
                <span className="flex-1 truncate text-sm font-medium text-slate-800">
                  {e.displayName || e.username || e.accountId.slice(0, 8)}
                  {isMe && <span className="ml-2 text-xs text-brand-600">you</span>}
                </span>
                {e.level != null && (
                  <span className="badge bg-slate-100 text-slate-500">Lv {e.level}</span>
                )}
                <span className="w-20 shrink-0 text-right text-sm font-semibold text-slate-900">
                  {e.points.toLocaleString()} pts
                </span>
              </li>
            );
          })}
        </ol>
      )}
    </>
  );
}
