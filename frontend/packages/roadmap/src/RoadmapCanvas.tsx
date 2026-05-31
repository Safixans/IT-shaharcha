"use client";

import { useState } from "react";
import {
  layoutRoadmap,
  type RoadmapEdgeInput,
  type RoadmapNodeInput,
} from "./layout";

export interface RoadmapCanvasProps {
  nodes: RoadmapNodeInput[];
  edges: RoadmapEdgeInput[];
  done: Set<string>;
  onToggle: (nodeKey: string) => void;
  /** Builds the href for a node's linked course. Defaults to /learn/courses/{id}. */
  courseHref?: (courseId: string) => string;
}

const defaultCourseHref = (courseId: string) => `/learn/courses/${courseId}`;

export function RoadmapCanvas({
  nodes,
  edges,
  done,
  onToggle,
  courseHref = defaultCourseHref,
}: RoadmapCanvasProps) {
  const { items } = layoutRoadmap(nodes, edges);

  return (
    <div className="relative mx-auto max-w-3xl pb-4">
      {/* central spine */}
      <div className="pointer-events-none absolute left-1/2 top-0 bottom-0 w-0.5 -translate-x-1/2 bg-slate-300" />

      <ul className="relative space-y-5">
        {items.map(({ node, role, side }) => {
          const isDone = done.has(node.nodeKey);

          if (role === "milestone") {
            return (
              <li key={node.nodeKey} className="relative flex justify-center">
                <MilestoneNote
                  node={node}
                  done={isDone}
                  onToggle={() => onToggle(node.nodeKey)}
                  courseHref={courseHref}
                />
              </li>
            );
          }

          if (role === "optional") {
            return (
              <li
                key={node.nodeKey}
                className={`relative flex items-center ${
                  side === "left" ? "justify-start pr-[52%]" : "justify-end pl-[52%]"
                }`}
              >
                <span
                  className={`pointer-events-none absolute top-1/2 h-0 border-t-2 border-dotted border-slate-400 ${
                    side === "left" ? "left-[48%] right-1/2" : "left-1/2 right-[48%]"
                  }`}
                />
                <FlowNode
                  node={node}
                  done={isDone}
                  onToggle={() => onToggle(node.nodeKey)}
                  courseHref={courseHref}
                  variant="optional"
                />
              </li>
            );
          }

          return (
            <li key={node.nodeKey} className="relative flex justify-center">
              <FlowNode
                node={node}
                done={isDone}
                onToggle={() => onToggle(node.nodeKey)}
                courseHref={courseHref}
                variant="core"
              />
            </li>
          );
        })}
      </ul>
    </div>
  );
}

function DoneBadge({
  done,
  optional,
  onToggle,
}: {
  done: boolean;
  optional?: boolean;
  onToggle: () => void;
}) {
  return (
    <button
      type="button"
      onClick={(e) => {
        e.stopPropagation();
        onToggle();
      }}
      aria-pressed={done}
      title={done ? "Mark as not done" : "Mark as done"}
      className={`absolute -right-2.5 -top-2.5 z-10 grid h-6 w-6 place-items-center rounded-full border-2 text-[11px] font-bold transition-colors ${
        done
          ? "border-emerald-600 bg-emerald-500 text-white"
          : optional
            ? "border-accent-500 bg-white text-transparent hover:text-accent-400"
            : "border-slate-400 bg-white text-transparent hover:text-slate-400"
      }`}
    >
      ✓
    </button>
  );
}

function FlowNode({
  node,
  done,
  onToggle,
  courseHref,
  variant,
}: {
  node: RoadmapNodeInput;
  done: boolean;
  onToggle: () => void;
  courseHref: (courseId: string) => string;
  variant: "core" | "optional";
}) {
  const [open, setOpen] = useState(false);
  const expandable = Boolean(node.summary || node.detail || node.courseId);

  const base =
    "relative w-full rounded-md border-2 border-slate-900 px-4 py-2.5 text-center shadow-[3px_3px_0_0_rgb(15_23_42)] transition-transform";
  const color = variant === "core" ? "bg-[#ffdf3d]" : "bg-[#fbe5a6]";
  const width = variant === "core" ? "max-w-xs" : "max-w-[15rem]";

  return (
    <div className={`${width} w-full`}>
      <div
        role={expandable ? "button" : undefined}
        tabIndex={expandable ? 0 : undefined}
        onClick={() => expandable && setOpen((v) => !v)}
        onKeyDown={(e) => {
          if (expandable && (e.key === "Enter" || e.key === " ")) {
            e.preventDefault();
            setOpen((v) => !v);
          }
        }}
        className={`${base} ${color} ${expandable ? "cursor-pointer hover:-translate-y-0.5" : ""} ${
          done ? "opacity-80" : ""
        }`}
      >
        <DoneBadge done={done} optional={variant === "optional"} onToggle={onToggle} />
        <span className="text-sm font-semibold text-slate-900">{node.title}</span>
      </div>

      {open && (
        <div className="mt-2 rounded-xl border border-slate-200 bg-white p-4 text-left shadow-soft">
          {node.summary && <p className="text-sm font-medium text-slate-800">{node.summary}</p>}
          {node.detail && (
            <p className="mt-2 text-sm leading-relaxed text-slate-600">{node.detail}</p>
          )}
          {node.courseId && (
            <a href={courseHref(node.courseId)} className="btn-primary btn-sm mt-3 inline-flex">
              Open {node.courseTitle ?? "course"}
            </a>
          )}
        </div>
      )}
    </div>
  );
}

function MilestoneNote({
  node,
  done,
  onToggle,
  courseHref,
}: {
  node: RoadmapNodeInput;
  done: boolean;
  onToggle: () => void;
  courseHref: (courseId: string) => string;
}) {
  return (
    <div className="relative w-full max-w-md rounded-xl border border-slate-200 border-l-4 border-l-amber-400 bg-white p-4 shadow-soft">
      <DoneBadge done={done} onToggle={onToggle} />
      <div className="flex items-center gap-2">
        <span className="badge-amber">Milestone</span>
        <h3 className="text-sm font-semibold text-slate-900">{node.title}</h3>
      </div>
      {node.summary && <p className="mt-2 text-sm text-slate-600">{node.summary}</p>}
      {node.detail && <p className="mt-2 text-sm leading-relaxed text-slate-500">{node.detail}</p>}
      {node.courseId && (
        <a href={courseHref(node.courseId)} className="btn-primary btn-sm mt-3 inline-flex">
          Open {node.courseTitle ?? "course"}
        </a>
      )}
    </div>
  );
}
