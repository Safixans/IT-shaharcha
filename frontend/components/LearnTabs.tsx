"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const tabs = [
  { href: "/learn", label: "Courses" },
  { href: "/learn/tutorials", label: "Tutorials" },
  { href: "/learn/docs", label: "Docs" },
  { href: "/learn/typing", label: "Typing" },
  { href: "/learn/progress", label: "My progress" },
];

export default function LearnTabs() {
  const pathname = usePathname();

  function isActive(href: string): boolean {
    if (href === "/learn") return pathname === "/learn" || pathname.startsWith("/learn/courses");
    return pathname.startsWith(href);
  }

  return (
    <nav className="flex flex-wrap gap-2 border-b border-slate-200 pb-3">
      {tabs.map((t) => (
        <Link
          key={t.href}
          href={t.href}
          className={
            isActive(t.href)
              ? "rounded-lg bg-brand-50 px-3 py-1.5 text-sm font-medium text-brand-600"
              : "rounded-lg px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100"
          }
        >
          {t.label}
        </Link>
      ))}
    </nav>
  );
}
