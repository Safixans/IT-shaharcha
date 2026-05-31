"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import {
  api,
  ApiError,
  type Choice,
  type ExamDetail,
  type ExamType,
  type QuestionInput,
  type QuestionKind,
  type Section,
} from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../../components/ui";
import { isAdmin } from "@itsh/auth";

const EXAM_TYPES: ExamType[] = ["IELTS", "SAT", "MOCK", "PRACTICE"];

const KINDS: QuestionKind[] = [
  "single_choice",
  "multiple_choice",
  "true_false",
  "short_answer",
  "essay",
];

const NEEDS_CHOICES = (k: QuestionKind) => k === "single_choice" || k === "multiple_choice";

function parseChoices(raw: string): Choice[] {
  // Each non-empty line "A=Four" or "A|Four" -> { id: "A", text: "Four" }
  return raw
    .split("\n")
    .map((l) => l.trim())
    .filter(Boolean)
    .map((l) => {
      const sep = l.includes("=") ? "=" : "|";
      const [id, ...rest] = l.split(sep);
      return { id: id.trim(), text: rest.join(sep).trim() };
    });
}

export default function ExamBuilder({ params }: { params: Promise<{ examId: string }> }) {
  const { examId } = use(params);
  const [exam, setExam] = useState<ExamDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [admin, setAdmin] = useState(false);
  const [sectionName, setSectionName] = useState("");

  useEffect(() => setAdmin(isAdmin()), []);

  async function load() {
    setError(null);
    try {
      setExam(await api.getExam(examId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load exam.");
    }
  }

  useEffect(() => {
    void load();
  }, [examId]);

  function fail(err: unknown) {
    setError(err instanceof ApiError ? err.message : "Action failed.");
  }

  async function addSection(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.createSection(examId, { name: sectionName, order: exam?.sections.length ?? 0 });
      setSectionName("");
      await load();
    } catch (err) {
      fail(err);
    }
  }

  if (exam === null && !error) return <Loading />;

  return (
    <>
      <PageHeader
        title={exam?.title ?? "Exam"}
        description={exam ? `${exam.examType} · ${exam.sections.length} section(s)` : undefined}
        action={
          <Link href="/assessment" className="btn-ghost">
            ← All exams
          </Link>
        }
      />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      {exam && <ExamSettings exam={exam} onSaved={load} onError={fail} />}

      <form onSubmit={addSection} className="card mb-6 flex items-end gap-3">
        <div className="flex-1">
          <Field label="Add a section">
            <input className="input" value={sectionName} onChange={(e) => setSectionName(e.target.value)} placeholder="e.g. Reading" required />
          </Field>
        </div>
        <button className="btn-primary">Add section</button>
      </form>

      <div className="space-y-4">
        {exam?.sections.length === 0 && (
          <div className="card text-sm text-slate-400">No sections yet — add one above.</div>
        )}
        {exam?.sections.map((s) => (
          <SectionCard key={s.id} section={s} admin={admin} onChange={load} onError={fail} />
        ))}
      </div>
    </>
  );
}

function SectionCard({
  section,
  admin,
  onChange,
  onError,
}: {
  section: Section;
  admin: boolean;
  onChange: () => void;
  onError: (e: unknown) => void;
}) {
  const [open, setOpen] = useState(false);
  const [prompt, setPrompt] = useState("");
  const [kind, setKind] = useState<QuestionKind>("single_choice");
  const [points, setPoints] = useState("1");
  const [choices, setChoices] = useState("A=\nB=");
  const [correct, setCorrect] = useState("");
  const [busy, setBusy] = useState(false);

  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(section.content ?? "");
  const [pdfUrl, setPdfUrl] = useState(section.pdfUrl ?? "");
  const [savingMaterial, setSavingMaterial] = useState(false);

  async function saveMaterial(e: React.FormEvent) {
    e.preventDefault();
    setSavingMaterial(true);
    try {
      await api.updateSection(section.id, {
        name: section.name,
        order: section.order,
        durationMinutes: section.durationMinutes ?? undefined,
        content: content.trim() || undefined,
        pdfUrl: pdfUrl.trim() || undefined,
      });
      setEditing(false);
      onChange();
    } catch (err) {
      onError(err);
    } finally {
      setSavingMaterial(false);
    }
  }

  const hasMaterial = Boolean(section.content || section.pdfUrl);

  async function addQuestion(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      const body: QuestionInput = {
        prompt,
        kind,
        points: points ? Number(points) : 1,
      };
      if (NEEDS_CHOICES(kind)) {
        body.choices = parseChoices(choices);
        body.correctAnswer = correct || undefined;
      } else if (correct) {
        body.correctAnswer = correct;
      }
      await api.createQuestion(section.id, body);
      setPrompt("");
      setCorrect("");
      setChoices("A=\nB=");
      setOpen(false);
      onChange();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <div>
          <p className="font-medium text-slate-900">{section.name}</p>
          <p className="text-xs text-slate-500">
            {section.questionCount} question{section.questionCount === 1 ? "" : "s"}
          </p>
        </div>
        <div className="flex gap-2">
          <button className="btn-ghost btn-sm" onClick={() => setEditing((v) => !v)}>
            {editing ? "Close" : hasMaterial ? "Edit material" : "Add material"}
          </button>
          <button className="btn-ghost btn-sm" onClick={() => setOpen((v) => !v)}>
            {open ? "Close" : "Add question"}
          </button>
          {admin && (
            <button className="btn-danger btn-sm" onClick={() => api.deleteSection(section.id).then(onChange).catch(onError)}>
              Delete
            </button>
          )}
        </div>
      </div>

      {hasMaterial && !editing && (
        <div className="mt-3 rounded-lg border border-slate-100 bg-slate-50 px-3 py-2 text-xs text-slate-500">
          {section.content && <p className="line-clamp-2 whitespace-pre-wrap">{section.content}</p>}
          {section.pdfUrl && <p className="mt-1 truncate font-medium text-brand-600">PDF: {section.pdfUrl}</p>}
        </div>
      )}

      {editing && (
        <form onSubmit={saveMaterial} className="mt-4 space-y-3 border-t border-slate-100 pt-4">
          <Field label="Reading material / information" hint="Shown to the test-taker before the questions. Plain text — paste a passage or instructions.">
            <textarea
              className="input min-h-32"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="e.g. Read the passage below and answer the questions that follow…"
            />
          </Field>
          <Field label="PDF URL (optional)" hint="A link to a PDF — embedded inline for the test-taker, no download needed.">
            <input
              className="input"
              type="url"
              value={pdfUrl}
              onChange={(e) => setPdfUrl(e.target.value)}
              placeholder="https://…/reading.pdf"
            />
          </Field>
          <button className="btn-primary" disabled={savingMaterial}>
            {savingMaterial ? "Saving…" : "Save material"}
          </button>
        </form>
      )}

      {open && (
        <form onSubmit={addQuestion} className="mt-4 space-y-3 border-t border-slate-100 pt-4">
          <Field label="Prompt">
            <input className="input" value={prompt} onChange={(e) => setPrompt(e.target.value)} required />
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field label="Kind">
              <select className="select" value={kind} onChange={(e) => setKind(e.target.value as QuestionKind)}>
                {KINDS.map((k) => (
                  <option key={k} value={k}>
                    {k.replace("_", " ")}
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Points">
              <input className="input" type="number" min={0} value={points} onChange={(e) => setPoints(e.target.value)} />
            </Field>
          </div>
          {NEEDS_CHOICES(kind) && (
            <Field label="Choices" hint='One per line as "id=text", e.g. A=Four'>
              <textarea className="input min-h-24 font-mono text-xs" value={choices} onChange={(e) => setChoices(e.target.value)} />
            </Field>
          )}
          <Field label="Correct answer" hint={NEEDS_CHOICES(kind) ? "Choice id, e.g. A" : "Expected answer (optional)"}>
            <input className="input" value={correct} onChange={(e) => setCorrect(e.target.value)} />
          </Field>
          <button className="btn-primary" disabled={busy}>
            {busy ? "Adding…" : "Add question"}
          </button>
        </form>
      )}
    </div>
  );
}

function ExamSettings({
  exam,
  onSaved,
  onError,
}: {
  exam: ExamDetail;
  onSaved: () => void;
  onError: (e: unknown) => void;
}) {
  const [open, setOpen] = useState(false);
  const [title, setTitle] = useState(exam.title);
  const [examType, setExamType] = useState<ExamType>(exam.examType);
  const [duration, setDuration] = useState(exam.durationMinutes?.toString() ?? "");
  const [isReal, setIsReal] = useState(exam.isRealExam);
  const [busy, setBusy] = useState(false);

  async function save(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      await api.updateExam(exam.id, {
        title,
        examType,
        durationMinutes: duration ? Number(duration) : undefined,
        isRealExam: isReal,
      });
      setOpen(false);
      onSaved();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="card mb-6">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-slate-700">Exam settings</p>
        <button className="btn-ghost btn-sm" onClick={() => setOpen((v) => !v)}>
          {open ? "Close" : "Edit"}
        </button>
      </div>
      {open && (
        <form onSubmit={save} className="mt-4 space-y-3 border-t border-slate-100 pt-4">
          <Field label="Title">
            <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field label="Type">
              <select className="select" value={examType} onChange={(e) => setExamType(e.target.value as ExamType)}>
                {EXAM_TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Duration (minutes)">
              <input className="input" type="number" min={0} value={duration} onChange={(e) => setDuration(e.target.value)} />
            </Field>
          </div>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input type="checkbox" checked={isReal} onChange={(e) => setIsReal(e.target.checked)} />
            Real (graded) exam
          </label>
          <button className="btn-primary" disabled={busy}>
            {busy ? "Saving…" : "Save settings"}
          </button>
        </form>
      )}
    </div>
  );
}
