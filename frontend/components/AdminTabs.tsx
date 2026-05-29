"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const tabs = [
  { href: "/admin/learning", label: "Tracks & courses" },
  { href: "/admin/learning/tutorials", label: "Tutorials" },
  { href: "/admin/learning/docs", label: "Docs" },
  { href: "/admin/learning/typing", label: "Typing" },
  { href: "/admin/learning/sources", label: "Sources" },
];

export default function AdminTabs() {
  const pathname = usePathname();

  function isActive(href: string): boolean {
    if (href === "/admin/learning") {
      return pathname === "/admin/learning" || pathname.startsWith("/admin/learning/courses");
    }
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
