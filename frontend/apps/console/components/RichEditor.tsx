"use client";

import { useEffect, useRef, useState } from "react";
import { api } from "@itsh/api-client";

/**
 * A lightweight contentEditable editor for authoring IELTS section HTML. The toolbar inserts the
 * four answer "blots" (text / single-choice / dropdown / multi-select), tables, and images
 * (uploaded to the attachment service and embedded as <attachment-blot>). The serialized innerHTML
 * is exactly what the backend parser consumes — answers live in the blot markup, so this round-
 * trips a unit's originalSectionData for editing. Styling is class/tag based (see globals.css).
 */
type Panel = null | "input" | "radio" | "select" | "multi" | "table";
type Opt = { text: string; correct: boolean };

const esc = (s: string) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
const escAttr = (s: string) => esc(s).replace(/"/g, "&quot;");

export function RichEditor({
  initialHtml,
  onChange,
  onError,
}: {
  initialHtml: string;
  onChange: (html: string) => void;
  onError?: (e: unknown) => void;
}) {
  const surfaceRef = useRef<HTMLDivElement | null>(null);
  const savedRange = useRef<Range | null>(null);
  const [panel, setPanel] = useState<Panel>(null);
  const [uploading, setUploading] = useState(false);

  // Seed once; the parent remounts (via key) to load different content for editing.
  useEffect(() => {
    if (surfaceRef.current) surfaceRef.current.innerHTML = initialHtml || "<p><br></p>";
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const emit = () => onChange(surfaceRef.current?.innerHTML ?? "");

  const saveSelection = () => {
    const sel = window.getSelection();
    if (sel && sel.rangeCount && surfaceRef.current?.contains(sel.anchorNode)) {
      savedRange.current = sel.getRangeAt(0).cloneRange();
    }
  };

  const insertHTML = (html: string) => {
    const surface = surfaceRef.current;
    if (!surface) return;
    surface.focus();
    const sel = window.getSelection();
    let range = savedRange.current;
    if (!range || !surface.contains(range.commonAncestorContainer)) {
      range = document.createRange();
      range.selectNodeContents(surface);
      range.collapse(false);
    }
    const tpl = document.createElement("template");
    tpl.innerHTML = html;
    const frag = tpl.content;
    const last = frag.lastChild;
    range.deleteContents();
    range.insertNode(frag);
    if (last && sel) {
      const after = document.createRange();
      after.setStartAfter(last);
      after.collapse(true);
      sel.removeAllRanges();
      sel.addRange(after);
      savedRange.current = after.cloneRange();
    }
    emit();
  };

  const exec = (command: string, value?: string) => {
    surfaceRef.current?.focus();
    document.execCommand(command, false, value);
    emit();
  };

  const openPanel = (p: Panel) => {
    saveSelection();
    setPanel(p);
  };

  const insertImage = async (file: File) => {
    setUploading(true);
    try {
      const ref = await api.uploadAttachment(file);
      insertHTML(
        `<attachment-blot data-file-id="${escAttr(ref.fileId)}" data-name="${escAttr(ref.originalName)}">${esc(
          ref.originalName,
        )}</attachment-blot>&nbsp;`,
      );
    } catch (e) {
      onError?.(e);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="itsh-editor">
      <div className="itsh-toolbar">
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => exec("formatBlock", "p")}>
          ¶
        </button>
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => exec("formatBlock", "h3")}>
          H
        </button>
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => exec("bold")}>
          <b>B</b>
        </button>
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => exec("insertUnorderedList")}>
          • List
        </button>
        <span className="mx-1 w-px self-stretch bg-slate-200" />
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => openPanel("input")}>
          + Blank
        </button>
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => openPanel("radio")}>
          + Single
        </button>
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => openPanel("multi")}>
          + Multi
        </button>
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => openPanel("select")}>
          + Dropdown
        </button>
        <button type="button" onMouseDown={(e) => e.preventDefault()} onClick={() => openPanel("table")}>
          + Table
        </button>
        <label className="cursor-pointer rounded-md border border-slate-200 bg-white px-2 py-0.5 text-xs text-slate-600 hover:bg-indigo-50">
          {uploading ? "Uploading…" : "+ Image"}
          <input
            type="file"
            accept="image/*"
            className="hidden"
            disabled={uploading}
            onChange={(e) => {
              const f = e.target.files?.[0];
              if (f) void insertImage(f);
              e.target.value = "";
            }}
          />
        </label>
      </div>

      <div
        ref={surfaceRef}
        className="itsh-surface"
        contentEditable
        suppressContentEditableWarning
        onInput={emit}
        onKeyUp={saveSelection}
        onMouseUp={saveSelection}
      />

      {panel === "input" && (
        <InputPanel
          onCancel={() => setPanel(null)}
          onInsert={(answers) => {
            insertHTML(`<input type="text" value="${escAttr(answers)}" />&nbsp;`);
            setPanel(null);
          }}
        />
      )}
      {(panel === "radio" || panel === "select" || panel === "multi") && (
        <ChoicePanel
          kind={panel}
          onCancel={() => setPanel(null)}
          onInsert={(opts) => {
            insertHTML(buildChoiceBlot(panel, opts));
            setPanel(null);
          }}
        />
      )}
      {panel === "table" && (
        <TablePanel
          onCancel={() => setPanel(null)}
          onInsert={(rows, cols) => {
            insertHTML(buildTable(rows, cols));
            setPanel(null);
          }}
        />
      )}
    </div>
  );
}

// ---- blot builders ----

function buildChoiceBlot(kind: "radio" | "select" | "multi", opts: Opt[]): string {
  const clean = opts.filter((o) => o.text.trim());
  if (kind === "select") {
    const correct = clean.find((o) => o.correct)?.text ?? "";
    const inner = clean.map((o) => `<option value="${escAttr(o.text)}">${esc(o.text)}</option>`).join("");
    return `<select-blot data-correct-option="${escAttr(correct)}">${inner}</select-blot>&nbsp;`;
  }
  if (kind === "radio") {
    const correct = clean.find((o) => o.correct)?.text ?? "";
    const inner = clean
      .map((o) => `<label><input type="radio" value="${escAttr(o.text)}" /> ${esc(o.text)}</label>`)
      .join(" ");
    return `<radio-blot data-correct-option="${escAttr(correct)}">${inner}</radio-blot>&nbsp;`;
  }
  const correct = clean.filter((o) => o.correct).map((o) => o.text);
  const inner = clean
    .map((o) => `<label><input type="checkbox" value="${escAttr(o.text)}" /> ${esc(o.text)}</label>`)
    .join(" ");
  return `<checkbox-blot data-correct-options='${escAttr(JSON.stringify(correct))}'>${inner}</checkbox-blot>&nbsp;`;
}

function buildTable(rows: number, cols: number): string {
  let body = "";
  for (let r = 0; r < rows; r++) {
    let tds = "";
    for (let c = 0; c < cols; c++) tds += r === 0 ? "<th>Head</th>" : "<td><br /></td>";
    body += `<tr>${tds}</tr>`;
  }
  return `<table><tbody>${body}</tbody></table><p><br /></p>`;
}

// ---- panels ----

function InputPanel({ onInsert, onCancel }: { onInsert: (answers: string) => void; onCancel: () => void }) {
  const [answers, setAnswers] = useState("");
  return (
    <div className="itsh-panel space-y-2">
      <p className="font-medium text-slate-700">Fill-in-the-blank — acceptable answers</p>
      <input
        className="input"
        autoFocus
        value={answers}
        onChange={(e) => setAnswers(e.target.value)}
        placeholder="snails / a snail"
      />
      <p className="text-xs text-slate-400">Separate alternatives with “ / ” — any one counts as correct.</p>
      <div className="flex gap-2">
        <button type="button" className="btn-primary btn-sm" disabled={!answers.trim()} onClick={() => onInsert(answers.trim())}>
          Insert
        </button>
        <button type="button" className="btn-ghost btn-sm" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}

function ChoicePanel({
  kind,
  onInsert,
  onCancel,
}: {
  kind: "radio" | "select" | "multi";
  onInsert: (opts: Opt[]) => void;
  onCancel: () => void;
}) {
  const multi = kind === "multi";
  const [opts, setOpts] = useState<Opt[]>([
    { text: "", correct: true },
    { text: "", correct: false },
  ]);

  const setCorrect = (i: number) =>
    setOpts((s) => s.map((o, idx) => (multi ? (idx === i ? { ...o, correct: !o.correct } : o) : { ...o, correct: idx === i })));

  const valid = opts.some((o) => o.text.trim() && o.correct) && opts.filter((o) => o.text.trim()).length >= 2;

  return (
    <div className="itsh-panel space-y-2">
      <p className="font-medium text-slate-700">
        {kind === "radio" ? "Single choice" : kind === "select" ? "Dropdown" : "Multiple answers"} — options &amp;
        correct
      </p>
      {opts.map((o, i) => (
        <div key={i} className="flex items-center gap-2">
          <input
            type={multi ? "checkbox" : "radio"}
            checked={o.correct}
            onChange={() => setCorrect(i)}
            title="Mark correct"
          />
          <input
            className="input flex-1"
            value={o.text}
            placeholder={`Option ${i + 1}`}
            onChange={(e) => setOpts((s) => s.map((x, idx) => (idx === i ? { ...x, text: e.target.value } : x)))}
          />
          {opts.length > 2 && (
            <button
              type="button"
              className="text-xs text-slate-400 hover:text-red-600"
              onClick={() => setOpts((s) => s.filter((_, idx) => idx !== i))}
            >
              ✕
            </button>
          )}
        </div>
      ))}
      <div className="flex gap-2">
        <button
          type="button"
          className="text-xs text-brand-600 hover:text-brand-700"
          onClick={() => setOpts((s) => [...s, { text: "", correct: false }])}
        >
          + option
        </button>
      </div>
      <div className="flex gap-2">
        <button type="button" className="btn-primary btn-sm" disabled={!valid} onClick={() => onInsert(opts)}>
          Insert
        </button>
        <button type="button" className="btn-ghost btn-sm" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}

function TablePanel({ onInsert, onCancel }: { onInsert: (rows: number, cols: number) => void; onCancel: () => void }) {
  const [rows, setRows] = useState(3);
  const [cols, setCols] = useState(3);
  return (
    <div className="itsh-panel space-y-2">
      <p className="font-medium text-slate-700">Table — first row is a header</p>
      <div className="flex items-center gap-3 text-sm">
        <label className="flex items-center gap-1">
          Rows
          <input className="input w-16" type="number" min={1} max={20} value={rows} onChange={(e) => setRows(Number(e.target.value))} />
        </label>
        <label className="flex items-center gap-1">
          Cols
          <input className="input w-16" type="number" min={1} max={10} value={cols} onChange={(e) => setCols(Number(e.target.value))} />
        </label>
      </div>
      <div className="flex gap-2">
        <button type="button" className="btn-primary btn-sm" onClick={() => onInsert(rows, cols)}>
          Insert
        </button>
        <button type="button" className="btn-ghost btn-sm" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}
