import type { Metadata } from "next";
import Link from "next/link";
import { api, type Leaderboard, type ServiceDomain, type RankingPeriod } from "@itsh/api-client";
import { SiteHeader } from "../../components/SiteHeader";
import { SiteFooter } from "../../components/SiteFooter";

export const metadata: Metadata = {
  title: "Rankings",
  description: "See who's leading across IT-Shaharcha — overall and per domain.",
};

const DOMAINS: (ServiceDomain | "overall")[] = [
  "overall",
  "learning",
  "assessment",
  "portfolio",
];
const PERIODS: RankingPeriod[] = ["all_time", "monthly", "weekly", "daily"];

type Search = { domain?: string; period?: string };

async function load(domain?: ServiceDomain, period?: RankingPeriod): Promise<Leaderboard | null> {
  try {
    return await api.serverGetLeaderboard({ domain, period, size: 50 });
  } catch {
    return null;
  }
}

export default async function RankingsPage({
  searchParams,
}: {
  searchParams: Promise<Search>;
}) {
  const sp = await searchParams;
  const domain = (sp.domain as ServiceDomain | undefined) || undefined;
  const period = (PERIODS.includes(sp.period as RankingPeriod)
    ? (sp.period as RankingPeriod)
    : "all_time") as RankingPeriod;

  const board = await load(domain, period);
  const activeDomain = domain ?? "overall";

  return (
    <>
      <SiteHeader />
      <main className="flex-1">
        <div className="mx-auto max-w-3xl px-4 py-10">
          <h1 className="text-3xl font-bold text-slate-900">Rankings</h1>
          <p className="mt-1 text-slate-600">Top learners across the platform.</p>

          <div className="mt-6 flex flex-wrap gap-2">
            {DOMAINS.map((d) => {
              const href =
                d === "overall"
                  ? `/rankings?period=${period}`
                  : `/rankings?domain=${d}&period=${period}`;
              const active = activeDomain === d;
              return (
                <Link
                  key={d}
                  href={href}
                  className={`rounded-full px-3 py-1.5 text-sm font-medium capitalize ${
                    active
                      ? "bg-brand-500 text-white"
                      : "bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-100"
                  }`}
                >
                  {d}
                </Link>
              );
            })}
          </div>

          <div className="mt-3 flex flex-wrap gap-2">
            {PERIODS.map((p) => {
              const href =
                activeDomain === "overall"
                  ? `/rankings?period=${p}`
                  : `/rankings?domain=${activeDomain}&period=${p}`;
              const active = period === p;
              return (
                <Link
                  key={p}
                  href={href}
                  className={`text-xs font-medium ${
                    active ? "text-brand-600" : "text-slate-400 hover:text-slate-600"
                  }`}
                >
                  {p.replace("_", " ")}
                </Link>
              );
            })}
          </div>

          <div className="mt-6">
            {!board ? (
              <div className="card text-sm text-slate-500">
                Leaderboard is unavailable right now. Check back soon.
              </div>
            ) : board.entries.length === 0 ? (
              <div className="card text-sm text-slate-500">No ranked accounts yet.</div>
            ) : (
              <ol className="overflow-hidden rounded-xl border border-slate-200 bg-white">
                {board.entries.map((e) => (
                  <li
                    key={`${e.accountId}-${e.rank}`}
                    className="flex items-center gap-4 border-b border-slate-100 px-4 py-3 last:border-0"
                  >
                    <span className="w-8 shrink-0 text-center text-sm font-bold text-slate-400">
                      {e.rank}
                    </span>
                    <span className="flex-1 truncate text-sm font-medium text-slate-800">
                      {e.displayName || e.username || e.accountId.slice(0, 8)}
                    </span>
                    {e.level != null && (
                      <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-500">
                        Lv {e.level}
                      </span>
                    )}
                    <span className="w-20 shrink-0 text-right text-sm font-semibold text-slate-900">
                      {e.points.toLocaleString()} pts
                    </span>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
