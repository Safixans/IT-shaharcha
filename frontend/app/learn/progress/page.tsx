"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Nav from "@/components/Nav";
import LearnTabs from "@/components/LearnTabs";
import {
  api,
  ApiError,
  DomainAnalyticsSummary,
  Enrollment,
  TypingSession,
} from "@/lib/api";
import { isAuthenticated } from "@/lib/auth";

export default function ProgressPage() {
  const router = useRouter();
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [summary, setSummary] = useState<DomainAnalyticsSummary | null>(null);
  const [sessions, setSessions] = useState<TypingSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [e, s, ts] = await Promise.all([
        api.listMyEnrollments(),
        api.learningAnalyticsSummary().catch(() => null),
        api.listMyTypingSessions().catch(() => []),
      ]);
      setEnrollments(e);
      setSummary(s);
      setSessions(ts);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        router.replace("/login");
        return;
      }
      setError(err instanceof ApiError ? err.message : "Failed to load progress.");
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <>
        <Nav />
        <main className="mx-auto max-w-5xl px-4 py-16 text-slate-500">Loading…</main>
      </>
    );
  }

  const bestWpm = sessions.reduce((m, s) => Math.max(m, s.wpm), 0);

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-5xl space-y-6 px-4 py-10">
        <LearnTabs />
        <h1 className="text-2xl font-bold">My progress</h1>
        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <SummaryCard label="Points" value={summary?.points ?? 0} />
          <SummaryCard label="Courses" value={enrollments.length} />
          <SummaryCard
            label="Completed"
            value={enrollments.filter((e) => e.status === "completed").length}
          />
          <SummaryCard label="Best WPM" value={bestWpm} />
        </div>

        <section>
          <h2 className="mb-3 text-lg font-semibold">Enrolled courses</h2>
          {enrollments.length === 0 ? (
            <p className="text-slate-400">
              No enrollments yet.{" "}
              <Link href="/learn" className="text-brand-600 hover:underline">
                Browse courses
              </Link>
              .
            </p>
          ) : (
            <ul className="space-y-3">
              {enrollments.map((e) => (
                <li key={e.id} className="card">
                  <div className="flex items-center justify-between text-sm">
                    <Link
                      href={`/learn/courses/${e.courseId}`}
                      className="font-medium text-brand-600 hover:underline"
                    >
                      Course {e.courseId.slice(0, 8)}
                    </Link>
                    <span className="capitalize text-slate-500">
                      {e.status} · {e.progressPercent}%
                    </span>
                  </div>
                  <div className="mt-2 h-2 w-full rounded-full bg-slate-100">
                    <div
                      className="h-2 rounded-full bg-brand-500"
                      style={{ width: `${e.progressPercent}%` }}
                    />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section>
          <h2 className="mb-3 text-lg font-semibold">Recent typing sessions</h2>
          {sessions.length === 0 ? (
            <p className="text-slate-400">No typing sessions yet.</p>
          ) : (
            <div className="overflow-hidden rounded-xl border border-slate-200">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-left text-slate-500">
                  <tr>
                    <th className="px-4 py-2">When</th>
                    <th className="px-4 py-2">WPM</th>
                    <th className="px-4 py-2">Accuracy</th>
                    <th className="px-4 py-2">Duration</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {sessions.slice(0, 10).map((s) => (
                    <tr key={s.id}>
                      <td className="px-4 py-2 text-slate-500">
                        {new Date(s.createdAt).toLocaleString()}
                      </td>
                      <td className="px-4 py-2 font-medium">{s.wpm}</td>
                      <td className="px-4 py-2">{s.accuracyPercent}%</td>
                      <td className="px-4 py-2 text-slate-500">{s.durationSeconds}s</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </>
  );
}

function SummaryCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="card">
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <p className="mt-1 text-2xl font-bold text-slate-900">{value}</p>
    </div>
  );
}
