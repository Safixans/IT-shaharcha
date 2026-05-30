"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const TABS = [
  { href: "/learning", label: "Tracks & Courses" },
  { href: "/learning/content", label: "Content library" },
  { href: "/learning/sources", label: "Sources" },
];

export function LearningTabs() {
  const pathname = usePathname();
  return (
    <div className="mb-6 flex gap-1 border-b border-slate-200">
      {TABS.map((t) => {
        const active =
          t.href === "/learning"
            ? pathname === "/learning" || pathname.startsWith("/learning/courses")
            : pathname.startsWith(t.href);
        return (
          <Link
            key={t.href}
            href={t.href}
            className={`-mb-px border-b-2 px-3 py-2 text-sm font-medium ${
              active
                ? "border-brand-500 text-brand-700"
                : "border-transparent text-slate-500 hover:text-slate-800"
            }`}
          >
            {t.label}
          </Link>
        );
      })}
    </div>
  );
}
