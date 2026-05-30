"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, type Exam } from "@itsh/api-client";
import { Loading, PageHeader } from "../../../components/ui";

export default function ExamsPage() {
  const [exams, setExams] = useState<Exam[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .listExams({ size: 100 })
      .then((p) => setExams(p.items))
      .catch(() => setExams([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;

  return (
    <>
      <PageHeader title="Exams" description="Mock IELTS & SAT, plus practice tests." />
      {exams.length === 0 ? (
        <div className="card text-sm text-slate-400">No exams available yet.</div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {exams.map((x) => (
            <Link key={x.id} href={`/exams/${x.id}`} className="card transition-shadow hover:shadow-md">
              <div className="flex items-center justify-between">
                <p className="font-medium text-slate-900">{x.title}</p>
                <span className="badge bg-brand-50 text-brand-700">{x.examType}</span>
              </div>
              <p className="mt-2 text-xs text-slate-500">
                {x.sectionCount} section{x.sectionCount === 1 ? "" : "s"}
                {x.durationMinutes ? ` · ${x.durationMinutes} min` : ""}
              </p>
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
