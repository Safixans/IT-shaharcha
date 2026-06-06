"use client";

import { createElement, useMemo, useState, type ReactNode } from "react";
import { api } from "@itsh/api-client";

/**
 * Renders authored IELTS HTML (answer-stripped) with interactive controls bound to each blot's
 * data-problem-id. Submitted values are the option `value` text (or typed text), matching the
 * server's value-based grader. Surrounding HTML is rebuilt structurally (preserving class + table
 * attributes so the app's stylesheet can style by tag/class); the four blot elements become
 * controlled inputs, and <attachment-blot> becomes a server-download action button.
 */
export type AnswerMap = Record<string, string[]>;

const BLOT_TAGS = new Set(["select-blot", "radio-blot", "checkbox-blot"]);

const SAFE_TAGS = new Set([
  "p", "div", "span", "strong", "em", "b", "i", "u", "br", "ul", "ol", "li", "a",
  "h1", "h2", "h3", "h4", "h5", "h6", "table", "thead", "tbody", "tr", "td", "th",
  "blockquote", "section", "figure", "figcaption", "small", "hr",
]);

export function BlotForm({
  html,
  answers,
  onChange,
  disabled,
}: {
  html: string;
  answers: AnswerMap;
  onChange: (problemId: string, values: string[]) => void;
  disabled?: boolean;
}) {
  const dom = useMemo(() => {
    if (typeof window === "undefined") return null;
    return new DOMParser().parseFromString(`<body>${html}</body>`, "text/html").body;
  }, [html]);

  if (!dom) {
    return <div dangerouslySetInnerHTML={{ __html: html }} />;
  }

  let key = 0;
  const render = (node: Node): ReactNode => {
    if (node.nodeType === Node.TEXT_NODE) return node.textContent;
    if (node.nodeType !== Node.ELEMENT_NODE) return null;
    const el = node as Element;
    const tag = el.tagName.toLowerCase();
    const pid = el.getAttribute("data-problem-id");

    // Text input blot.
    if (tag === "input" && (el.getAttribute("type") ?? "text") === "text" && pid) {
      return (
        <input
          key={`b${key++}`}
          className="itsh-blank"
          disabled={disabled}
          value={answers[pid]?.[0] ?? ""}
          onChange={(e) => onChange(pid, e.target.value ? [e.target.value] : [])}
        />
      );
    }

    // Choice blots: options carry their value on child <option>/<input>.
    if (BLOT_TAGS.has(tag) && pid) {
      const opts = collectOptions(el);
      if (tag === "select-blot") {
        return (
          <select
            key={`b${key++}`}
            className="itsh-select"
            disabled={disabled}
            value={answers[pid]?.[0] ?? ""}
            onChange={(e) => onChange(pid, e.target.value ? [e.target.value] : [])}
          >
            <option value="">—</option>
            {opts.map((o, i) => (
              <option key={i} value={o}>
                {o}
              </option>
            ))}
          </select>
        );
      }
      const multi = tag === "checkbox-blot";
      const selected = answers[pid] ?? [];
      return (
        <span key={`b${key++}`} className="itsh-choices">
          {opts.map((o, i) => (
            <label key={i} className="itsh-choice">
              <input
                type={multi ? "checkbox" : "radio"}
                name={pid}
                disabled={disabled}
                checked={selected.includes(o)}
                onChange={() => {
                  if (multi) {
                    onChange(pid, selected.includes(o) ? selected.filter((x) => x !== o) : [...selected, o]);
                  } else {
                    onChange(pid, [o]);
                  }
                }}
              />
              {o}
            </label>
          ))}
        </span>
      );
    }

    // Attachment image: a button that resolves a presigned URL and opens it.
    if (tag === "attachment-blot") {
      const fileId = el.getAttribute("data-file-id");
      if (fileId) {
        return (
          <AttachmentChip
            key={`a${key++}`}
            fileId={fileId}
            name={el.getAttribute("data-name") ?? el.textContent ?? undefined}
          />
        );
      }
    }

    // Plain element: recurse, preserving class + table layout attributes.
    const children = Array.from(el.childNodes).map(render);
    const tagName = SAFE_TAGS.has(tag) ? tag : "span";
    const props: Record<string, unknown> = { key: `e${key++}` };
    const cls = el.getAttribute("class");
    if (cls) props.className = cls;
    const colspan = el.getAttribute("colspan");
    if (colspan) props.colSpan = Number(colspan);
    const rowspan = el.getAttribute("rowspan");
    if (rowspan) props.rowSpan = Number(rowspan);
    const scope = el.getAttribute("scope");
    if (scope) props.scope = scope;
    if (tagName === "a") {
      // Authored links open safely in a new tab; never trust the href for navigation chrome.
      props.href = el.getAttribute("href") ?? "#";
      props.target = "_blank";
      props.rel = "noopener noreferrer";
    }
    return createElement(tagName, props, children);
  };

  return <div className="itsh-content">{Array.from(dom.childNodes).map(render)}</div>;
}

function AttachmentChip({ fileId, name }: { fileId: string; name?: string }) {
  const [loading, setLoading] = useState(false);
  return (
    <button
      type="button"
      className="itsh-attachment"
      disabled={loading}
      onClick={async () => {
        setLoading(true);
        try {
          const url = await api.attachmentUrl(fileId);
          window.open(url, "_blank", "noopener,noreferrer");
        } finally {
          setLoading(false);
        }
      }}
    >
      {loading ? "Opening…" : `📎 ${name || "Open attachment"}`}
    </button>
  );
}

function collectOptions(el: Element): string[] {
  const out: string[] = [];
  el.querySelectorAll("option, input").forEach((child) => {
    const v = (child.getAttribute("value") ?? child.textContent ?? "").trim();
    if (v) out.push(v);
  });
  return out;
}
