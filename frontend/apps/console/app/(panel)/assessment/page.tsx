"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import {
  api,
  ApiError,
  type AttemptFamily,
  type ObjectiveQuestionInput,
  type ProblemType,
  type SatSection,
  type UnitDetail,
  type UnitMeta,
  type WritingTask,
} from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../components/ui";
import { RichEditor } from "../../../components/RichEditor";
import { canAuthor, isAdmin } from "@itsh/auth";

type FamilyKey = "listening" | "reading" | "writing" | "sat" | "quiz";

const TABS: { key: FamilyKey; label: string; gate: string }[] = [
  { key: "listening", label: "IELTS Listening", gate: "Activation needs exactly 40 questions." },
  { key: "reading", label: "IELTS Reading", gate: "Activation needs 13–14 questions." },
  { key: "writing", label: "IELTS Writing", gate: "Task 1 needs an image to activate." },
  { key: "sat", label: "SAT", gate: "Activation needs R&W 27 / Math 22 questions." },
  { key: "quiz", label: "Quizzes", gate: "Activation needs at least one question." },
];

function listFor(key: FamilyKey): Promise<UnitMeta[]> {
  const q = { size: 100 };
  const map: Record<FamilyKey, () => Promise<{ items: UnitMeta[] }>> = {
    listening: () => api.listListening(q),
    reading: () => api.listReading(q),
    writing: () => api.listWriting(q),
    sat: () => api.listSatModules(q),
    quiz: () => api.listQuizzes(q),
  };
  return map[key]().then((p) => p.items);
}

function activateFor(key: FamilyKey, id: string, active: boolean): Promise<unknown> {
  const map: Record<FamilyKey, () => Promise<unknown>> = {
    listening: () => api.activateListening(id, active),
    reading: () => api.activateReading(id, active),
    writing: () => api.activateWriting(id, active),
    sat: () => api.activateSat(id, active),
    quiz: () => api.activateQuiz(id, active),
  };
  return map[key]();
}

function deleteFor(key: FamilyKey, id: string): Promise<unknown> {
  const map: Record<FamilyKey, () => Promise<unknown>> = {
    listening: () => api.deleteListening(id),
    reading: () => api.deleteReading(id),
    writing: () => api.deleteWriting(id),
    sat: () => api.deleteSatModule(id),
    quiz: () => api.deleteQuiz(id),
  };
  return map[key]();
}

const parseTags = (raw: string): string[] =>
  raw.split(",").map((t) => t.trim()).filter(Boolean);

const msg = (e: unknown, fallback: string) => (e instanceof ApiError ? e.message : fallback);

export default function AssessmentConsole() {
  const [tab, setTab] = useState<FamilyKey>("listening");
  const [units, setUnits] = useState<UnitMeta[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [admin, setAdmin] = useState(false);
  const [editing, setEditing] = useState<UnitDetail | null>(null);

  useEffect(() => setAdmin(isAdmin()), []);

  const load = useCallback(() => {
    setUnits(null);
    setError(null);
    listFor(tab)
      .then(setUnits)
      .catch((e) => {
        setUnits([]);
        setError(msg(e, "Could not load units."));
      });
  }, [tab]);

  useEffect(() => load(), [load]);
  useEffect(() => setEditing(null), [tab]); // switching families cancels an in-flight edit

  const startEdit = (unit: UnitMeta) => {
    const loader = tab === "listening" ? api.getListening : api.getReading;
    loader(unit.id)
      .then((d) => {
        setEditing(d);
        window.scrollTo({ top: 0, behavior: "smooth" });
      })
      .catch((e) => setError(msg(e, "Could not load unit for editing.")));
  };

  const editable = tab === "listening" || tab === "reading";
  const tabMeta = TABS.find((t) => t.key === tab)!;

  return (
    <>
      <PageHeader
        title="Assessment"
        description="Author modular training: IELTS Listening / Reading / Writing, SAT modules, and quizzes."
        action={
          <Link href="/assessment/grading" className="btn-ghost">
            Writing grading →
          </Link>
        }
      />

      <div className="mb-6 flex flex-wrap gap-1 border-b border-slate-200">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium ${
              tab === t.key
                ? "border-brand-500 text-brand-700"
                : "border-transparent text-slate-500 hover:text-slate-700"
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {error && (
        <div className="mb-4">
          <ErrorBanner message={error} />
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-[1fr_22rem]">
        <div>
          {units === null ? (
            <Loading />
          ) : units.length === 0 ? (
            <div className="card text-sm text-slate-400">No units yet.</div>
          ) : (
            <ul className="space-y-2">
              {units.map((u) => (
                <UnitRow
                  key={u.id}
                  unit={u}
                  family={tab}
                  admin={admin}
                  onEdit={editable ? () => startEdit(u) : undefined}
                  onChanged={load}
                  onError={(e) => setError(msg(e, "Action failed."))}
                />
              ))}
            </ul>
          )}
        </div>

        {canAuthor() && (
          <div className="card h-fit space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="font-semibold text-slate-900">
                {editing ? "Edit" : "New"} {tabMeta.label} unit
              </h3>
              {editing && (
                <button className="btn-ghost btn-sm" onClick={() => setEditing(null)}>
                  Cancel edit
                </button>
              )}
            </div>
            <p className="-mt-1 text-xs text-slate-400">{tabMeta.gate}</p>
            <CreateForm
              key={editing ? editing.id : `new-${tab}`}
              family={tab}
              editing={editing}
              onSaved={() => {
                setEditing(null);
                load();
              }}
              onError={(e) => setError(msg(e, "Could not save unit."))}
            />
          </div>
        )}
      </div>
    </>
  );
}

function UnitRow({
  unit,
  family,
  admin,
  onEdit,
  onChanged,
  onError,
}: {
  unit: UnitMeta;
  family: FamilyKey;
  admin: boolean;
  onEdit?: () => void;
  onChanged: () => void;
  onError: (e: unknown) => void;
}) {
  const [busy, setBusy] = useState(false);

  const act = (fn: () => Promise<unknown>) => {
    setBusy(true);
    fn()
      .then(onChanged)
      .catch(onError)
      .finally(() => setBusy(false));
  };

  const sub: string[] = [`${unit.problemCount} q`];
  if (unit.satSection) sub.push(unit.satSection === "MATH" ? "Math" : "R&W");
  if (unit.writingTask) sub.push(unit.writingTask === "TASK_1" ? "Task 1" : "Task 2");
  if (unit.durationSeconds) sub.push(`${Math.round(unit.durationSeconds / 60)} min`);
  if (unit.tags?.length) sub.push(unit.tags.join(", "));

  return (
    <li className="card flex items-center justify-between gap-3">
      <div className="min-w-0">
        <div className="flex items-center gap-2">
          <span
            className={`badge ${unit.active ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-500"}`}
          >
            {unit.active ? "Active" : "Draft"}
          </span>
          <p className="truncate font-medium text-slate-900">{unit.title}</p>
        </div>
        <p className="mt-1 truncate text-xs text-slate-500">{sub.join(" · ")}</p>
      </div>
      <div className="flex shrink-0 gap-2">
        {onEdit && (
          <button className="btn-ghost btn-sm" disabled={busy} onClick={onEdit}>
            Edit
          </button>
        )}
        <button
          className="btn-ghost btn-sm"
          disabled={busy}
          onClick={() => act(() => activateFor(family, unit.id, !unit.active))}
        >
          {unit.active ? "Deactivate" : "Activate"}
        </button>
        {admin && (
          <button
            className="btn-danger btn-sm"
            disabled={busy}
            onClick={() => act(() => deleteFor(family, unit.id))}
          >
            Delete
          </button>
        )}
      </div>
    </li>
  );
}

// ---- create forms ----

function CreateForm({
  family,
  editing,
  onSaved,
  onError,
}: {
  family: FamilyKey;
  editing: UnitDetail | null;
  onSaved: () => void;
  onError: (e: unknown) => void;
}) {
  if (family === "listening" || family === "reading") {
    return <IeltsObjectiveForm family={family} editing={editing} onSaved={onSaved} onError={onError} />;
  }
  if (family === "writing") {
    return <WritingForm onCreated={onSaved} onError={onError} />;
  }
  return <ObjectiveForm family={family} onCreated={onSaved} onError={onError} />;
}

const BLOT_HINT =
  "Type the passage/questions; use the toolbar to insert blanks, choices, dropdowns, tables, and images. " +
  "A multi-answer question counts as that many marks.";

function IeltsObjectiveForm({
  family,
  editing,
  onSaved,
  onError,
}: {
  family: "listening" | "reading";
  editing: UnitDetail | null;
  onSaved: () => void;
  onError: (e: unknown) => void;
}) {
  const isEdit = !!editing;
  const [title, setTitle] = useState(editing?.title ?? "");
  const [tags, setTags] = useState((editing?.tags ?? []).join(", "));
  const [passage, setPassage] = useState(editing?.passage ?? "");
  const [audioId, setAudioId] = useState(editing?.audioId ?? "");
  const [duration, setDuration] = useState(editing?.durationSeconds ? String(editing.durationSeconds) : "");
  // The editor seeds from the answer-bearing originalSectionData when editing.
  const [questions, setQuestions] = useState(editing?.originalSectionData ?? "");
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!questions.trim()) {
      onError(new ApiError(400, "BAD_REQUEST", "Add at least one question."));
      return;
    }
    setBusy(true);
    try {
      const common = {
        title,
        tags: parseTags(tags),
        questions,
        durationSeconds: duration ? Number(duration) : undefined,
      };
      if (family === "listening") {
        const body = { ...common, audioId };
        if (isEdit) await api.updateListening(editing!.id, body);
        else await api.createListening(body);
      } else {
        const body = { ...common, passage: passage.trim() || undefined };
        if (isEdit) await api.updateReading(editing!.id, body);
        else await api.createReading(body);
      }
      onSaved();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <Field label="Title">
        <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
      </Field>
      <Field label="Tags" hint="Comma-separated, e.g. academic, part1">
        <input className="input" value={tags} onChange={(e) => setTags(e.target.value)} />
      </Field>
      {family === "listening" && (
        <Field label="Audio" hint="Upload the listening audio (mp3).">
          <FileUpload accept="audio/*" value={audioId} onUploaded={setAudioId} onError={onError} />
        </Field>
      )}
      {family === "reading" && (
        <Field label="Passage (optional)">
          <textarea
            className="input min-h-28"
            value={passage}
            onChange={(e) => setPassage(e.target.value)}
            placeholder="Reading passage shown alongside the questions…"
          />
        </Field>
      )}
      <Field label="Questions" hint={BLOT_HINT}>
        <RichEditor initialHtml={questions} onChange={setQuestions} onError={onError} />
      </Field>
      <Field label="Duration (minutes, optional)">
        <input
          className="input"
          type="number"
          min={1}
          value={duration ? String(Math.round(Number(duration) / 60)) : ""}
          onChange={(e) => setDuration(e.target.value ? String(Number(e.target.value) * 60) : "")}
        />
      </Field>
      <button className="btn-primary w-full" disabled={busy}>
        {busy ? "Saving…" : isEdit ? "Save changes" : "Create unit"}
      </button>
    </form>
  );
}

function WritingForm({ onCreated, onError }: { onCreated: () => void; onError: (e: unknown) => void }) {
  const [title, setTitle] = useState("");
  const [tags, setTags] = useState("");
  const [task, setTask] = useState<WritingTask>("TASK_2");
  const [prompt, setPrompt] = useState("");
  const [imageId, setImageId] = useState("");
  const [duration, setDuration] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      await api.createWriting({
        title,
        tags: parseTags(tags),
        task,
        prompt,
        imageId: imageId || undefined,
        durationSeconds: duration ? Number(duration) : undefined,
      });
      setTitle("");
      setTags("");
      setPrompt("");
      setImageId("");
      setDuration("");
      onCreated();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <Field label="Title">
        <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
      </Field>
      <Field label="Tags" hint="Comma-separated">
        <input className="input" value={tags} onChange={(e) => setTags(e.target.value)} />
      </Field>
      <Field label="Task">
        <select className="select" value={task} onChange={(e) => setTask(e.target.value as WritingTask)}>
          <option value="TASK_1">Task 1 (report / letter)</option>
          <option value="TASK_2">Task 2 (essay)</option>
        </select>
      </Field>
      <Field label="Prompt">
        <textarea
          className="input min-h-28"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          required
        />
      </Field>
      {task === "TASK_1" && (
        <Field label="Image" hint="Task 1 chart/diagram. Required to activate.">
          <FileUpload accept="image/*" value={imageId} onUploaded={setImageId} onError={onError} />
        </Field>
      )}
      <Field label="Duration (minutes, optional)">
        <input
          className="input"
          type="number"
          min={1}
          value={duration}
          onChange={(e) => setDuration(e.target.value ? String(Number(e.target.value) * 60) : "")}
        />
      </Field>
      <button className="btn-primary w-full" disabled={busy}>
        {busy ? "Creating…" : "Create unit"}
      </button>
    </form>
  );
}

function ObjectiveForm({
  family,
  onCreated,
  onError,
}: {
  family: "sat" | "quiz";
  onCreated: () => void;
  onError: (e: unknown) => void;
}) {
  const [title, setTitle] = useState("");
  const [tags, setTags] = useState("");
  const [section, setSection] = useState<SatSection>("READING_WRITING");
  const [duration, setDuration] = useState("");
  const [questions, setQuestions] = useState<ObjectiveQuestionInput[]>([]);
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (questions.length === 0) {
      onError(new ApiError(400, "BAD_REQUEST", "Add at least one question."));
      return;
    }
    setBusy(true);
    try {
      const body = {
        title,
        tags: parseTags(tags),
        durationSeconds: duration ? Number(duration) : undefined,
        questions,
        ...(family === "sat" ? { satSection: section } : {}),
      };
      if (family === "sat") await api.createSatModule(body);
      else await api.createQuiz(body);
      setTitle("");
      setTags("");
      setDuration("");
      setQuestions([]);
      onCreated();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <Field label="Title">
        <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
      </Field>
      <Field label="Tags" hint="Comma-separated">
        <input className="input" value={tags} onChange={(e) => setTags(e.target.value)} />
      </Field>
      {family === "sat" && (
        <Field label="Section">
          <select className="select" value={section} onChange={(e) => setSection(e.target.value as SatSection)}>
            <option value="READING_WRITING">Reading & Writing</option>
            <option value="MATH">Math</option>
          </select>
        </Field>
      )}
      <Field label="Duration (minutes, optional)">
        <input
          className="input"
          type="number"
          min={1}
          value={duration}
          onChange={(e) => setDuration(e.target.value ? String(Number(e.target.value) * 60) : "")}
        />
      </Field>
      <ObjectiveBuilder questions={questions} onChange={setQuestions} />
      <button className="btn-primary w-full" disabled={busy}>
        {busy ? "Creating…" : `Create unit (${questions.length} q)`}
      </button>
    </form>
  );
}

const PROBLEM_TYPES: { value: ProblemType; label: string }[] = [
  { value: "RADIO", label: "Single choice" },
  { value: "MULTI_SELECT", label: "Multiple answers" },
  { value: "SELECT", label: "Dropdown" },
  { value: "INPUT", label: "Short answer" },
];

function ObjectiveBuilder({
  questions,
  onChange,
}: {
  questions: ObjectiveQuestionInput[];
  onChange: (q: ObjectiveQuestionInput[]) => void;
}) {
  const update = (i: number, patch: Partial<ObjectiveQuestionInput>) =>
    onChange(questions.map((q, idx) => (idx === i ? { ...q, ...patch } : q)));

  const add = () =>
    onChange([
      ...questions,
      { type: "RADIO", prompt: "", options: [{ text: "", correct: true }, { text: "", correct: false }] },
    ]);

  return (
    <div className="space-y-3 rounded-lg border border-slate-200 p-3">
      <p className="text-sm font-medium text-slate-700">Questions</p>
      {questions.map((q, i) => (
        <div key={i} className="space-y-2 rounded-md border border-slate-100 bg-slate-50 p-2">
          <div className="flex gap-2">
            <select
              className="select"
              value={q.type}
              onChange={(e) => {
                const type = e.target.value as ProblemType;
                update(i, {
                  type,
                  options:
                    type === "INPUT"
                      ? undefined
                      : q.options ?? [{ text: "", correct: true }, { text: "", correct: false }],
                  correctAnswers: type === "INPUT" ? q.correctAnswers ?? [""] : undefined,
                });
              }}
            >
              {PROBLEM_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
            <button
              type="button"
              className="btn-ghost btn-sm text-red-600"
              onClick={() => onChange(questions.filter((_, idx) => idx !== i))}
            >
              Remove
            </button>
          </div>
          <input
            className="input"
            placeholder="Question prompt"
            value={q.prompt}
            onChange={(e) => update(i, { prompt: e.target.value })}
            required
          />
          {q.type === "INPUT" ? (
            <input
              className="input"
              placeholder="Acceptable answers, comma-separated"
              value={(q.correctAnswers ?? []).join(", ")}
              onChange={(e) => update(i, { correctAnswers: parseTags(e.target.value) })}
            />
          ) : (
            <OptionEditor
              multi={q.type === "MULTI_SELECT"}
              options={q.options ?? []}
              onChange={(options) => update(i, { options })}
            />
          )}
        </div>
      ))}
      <button type="button" className="btn-ghost btn-sm w-full" onClick={add}>
        + Add question
      </button>
    </div>
  );
}

function OptionEditor({
  options,
  multi,
  onChange,
}: {
  options: { text: string; correct: boolean }[];
  multi: boolean;
  onChange: (o: { text: string; correct: boolean }[]) => void;
}) {
  const setCorrect = (i: number) =>
    onChange(
      options.map((o, idx) =>
        multi ? (idx === i ? { ...o, correct: !o.correct } : o) : { ...o, correct: idx === i },
      ),
    );

  return (
    <div className="space-y-1.5">
      {options.map((o, i) => (
        <div key={i} className="flex items-center gap-2">
          <input
            type={multi ? "checkbox" : "radio"}
            checked={o.correct}
            onChange={() => setCorrect(i)}
            title="Mark correct"
          />
          <input
            className="input flex-1"
            placeholder={`Option ${i + 1}`}
            value={o.text}
            onChange={(e) =>
              onChange(options.map((x, idx) => (idx === i ? { ...x, text: e.target.value } : x)))
            }
            required
          />
          {options.length > 2 && (
            <button
              type="button"
              className="text-xs text-slate-400 hover:text-red-600"
              onClick={() => onChange(options.filter((_, idx) => idx !== i))}
            >
              ✕
            </button>
          )}
        </div>
      ))}
      <button
        type="button"
        className="text-xs text-brand-600 hover:text-brand-700"
        onClick={() => onChange([...options, { text: "", correct: false }])}
      >
        + Add option
      </button>
    </div>
  );
}

function FileUpload({
  accept,
  value,
  onUploaded,
  onError,
}: {
  accept: string;
  value: string;
  onUploaded: (fileId: string) => void;
  onError: (e: unknown) => void;
}) {
  const [busy, setBusy] = useState(false);
  return (
    <div className="space-y-1">
      <input
        type="file"
        accept={accept}
        className="text-xs"
        disabled={busy}
        onChange={async (e) => {
          const file = e.target.files?.[0];
          if (!file) return;
          setBusy(true);
          try {
            const ref = await api.uploadAttachment(file);
            onUploaded(ref.fileId);
          } catch (err) {
            onError(err);
          } finally {
            setBusy(false);
          }
        }}
      />
      {busy && <p className="text-xs text-slate-400">Uploading…</p>}
      {value && !busy && <p className="truncate text-xs text-green-600">Attached: {value}</p>}
    </div>
  );
}
