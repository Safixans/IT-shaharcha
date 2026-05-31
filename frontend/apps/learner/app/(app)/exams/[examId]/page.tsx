"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import {
  api,
  ApiError,
  type Answer,
  type ExamDetail,
  type ExamResult,
  type Section,
  type SessionQuestion,
} from "@itsh/api-client";
import { ErrorBanner, Loading, PageHeader } from "../../../../components/ui";

type Phase = "intro" | "taking" | "done";

export default function TakeExamPage({ params }: { params: Promise<{ examId: string }> }) {
  const { examId } = use(params);
  const [exam, setExam] = useState<ExamDetail | null>(null);
  const [phase, setPhase] = useState<Phase>("intro");
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [questions, setQuestions] = useState<SessionQuestion[]>([]);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [result, setResult] = useState<ExamResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.getExam(examId).then(setExam).catch((e) => setError(e instanceof ApiError ? e.message : "Load failed."));
  }, [examId]);

  async function start() {
    setBusy(true);
    setError(null);
    try {
      const session = await api.startExam(examId);
      setSessionId(session.id);
      setQuestions(await api.getSessionQuestions(session.id));
      setPhase("taking");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not start the exam.");
    } finally {
      setBusy(false);
    }
  }

  async function submit() {
    if (!sessionId) return;
    setBusy(true);
    setError(null);
    try {
      const payload: Answer[] = Object.entries(answers).map(([questionId, value]) => ({ questionId, value }));
      const r = await api.submitSession(sessionId, payload);
      setResult(r);
      setPhase("done");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not submit.");
    } finally {
      setBusy(false);
    }
  }

  if (exam === null && !error) return <Loading />;

  return (
    <>
      <PageHeader
        title={exam?.title ?? "Exam"}
        description={exam ? `${exam.examType}${exam.durationMinutes ? ` · ${exam.durationMinutes} min` : ""}` : undefined}
        action={
          <Link href="/exams" className="btn-ghost">
            ← Exams
          </Link>
        }
      />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      {phase === "intro" && (
        <div className="card">
          <p className="text-sm text-slate-600">
            {exam?.sectionCount} section{exam?.sectionCount === 1 ? "" : "s"}. Your answers are scored
            as soon as you submit.
          </p>
          <button className="btn-primary mt-4" onClick={start} disabled={busy}>
            {busy ? "Starting…" : "Start exam"}
          </button>
        </div>
      )}

      {phase === "taking" && (
        <div className="space-y-4">
          {exam?.sections
            .filter((s) => s.content || s.pdfUrl)
            .map((s) => (
              <SectionMaterial key={s.id} section={s} />
            ))}
          {questions.length === 0 && (
            <div className="card text-sm text-slate-400">This exam has no questions yet.</div>
          )}
          {questions.map((q, idx) => (
            <QuestionCard
              key={q.id}
              index={idx + 1}
              question={q}
              value={answers[q.id]}
              onChange={(v) => setAnswers((a) => ({ ...a, [q.id]: v }))}
            />
          ))}
          {questions.length > 0 && (
            <button className="btn-primary" onClick={submit} disabled={busy}>
              {busy ? "Submitting…" : "Submit exam"}
            </button>
          )}
        </div>
      )}

      {phase === "done" && result && (
        <div className="card">
          <p className="text-sm text-slate-500">Your score</p>
          <p className="mt-1 text-4xl font-bold text-slate-900">
            {result.scaledScore}
            {result.maxScore != null && <span className="text-xl text-slate-400"> / {result.maxScore}</span>}
          </p>
          {result.sectionScores && result.sectionScores.length > 0 && (
            <ul className="mt-4 space-y-1 text-sm text-slate-600">
              {result.sectionScores.map((s) => (
                <li key={s.section} className="flex justify-between">
                  <span>{s.section}</span>
                  <span className="font-medium text-slate-800">{s.score}</span>
                </li>
              ))}
            </ul>
          )}
          {result.feedback && <p className="mt-4 text-sm text-slate-600">{result.feedback}</p>}
          <div className="mt-6 flex gap-2">
            <Link href="/exams" className="btn-ghost">Back to exams</Link>
            <Link href="/" className="btn-primary">View progress</Link>
          </div>
        </div>
      )}
    </>
  );
}

function SectionMaterial({ section }: { section: Section }) {
  return (
    <div className="card border-brand-100 bg-brand-50/30">
      <p className="text-xs font-semibold uppercase tracking-wide text-brand-600">{section.name}</p>
      {section.content && (
        <p className="mt-2 whitespace-pre-wrap text-sm leading-relaxed text-slate-700">{section.content}</p>
      )}
      {section.pdfUrl && (
        <iframe
          src={section.pdfUrl}
          title={`${section.name} material`}
          className="mt-3 h-[28rem] w-full rounded-lg border border-slate-200 bg-white"
        />
      )}
    </div>
  );
}

function QuestionCard({
  index,
  question,
  value,
  onChange,
}: {
  index: number;
  question: SessionQuestion;
  value: string | undefined;
  onChange: (v: string) => void;
}) {
  return (
    <div className="card">
      <p className="font-medium text-slate-900">
        {index}. {question.prompt}
      </p>
      <div className="mt-3 space-y-2">
        {question.kind === "true_false" && (
          <Choices
            options={[
              { id: "true", text: "True" },
              { id: "false", text: "False" },
            ]}
            value={value}
            onChange={onChange}
          />
        )}
        {(question.kind === "single_choice" || question.kind === "multiple_choice") &&
          question.choices && (
            <Choices options={question.choices} value={value} onChange={onChange} />
          )}
        {(question.kind === "short_answer" || question.kind === "essay") && (
          <textarea
            className="input"
            rows={question.kind === "essay" ? 5 : 2}
            value={value ?? ""}
            onChange={(e) => onChange(e.target.value)}
            placeholder="Your answer…"
          />
        )}
      </div>
    </div>
  );
}

function Choices({
  options,
  value,
  onChange,
}: {
  options: { id: string; text: string }[];
  value: string | undefined;
  onChange: (v: string) => void;
}) {
  return (
    <div className="space-y-1.5">
      {options.map((o) => (
        <label key={o.id} className="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-sm hover:bg-slate-50">
          <input type="radio" checked={value === o.id} onChange={() => onChange(o.id)} />
          <span className="text-slate-700">{o.text}</span>
        </label>
      ))}
    </div>
  );
}
