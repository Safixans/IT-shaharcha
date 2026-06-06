"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";
import { api } from "@itsh/api-client";
import { getAccount, isAdmin } from "@itsh/auth";

type NavItem = { href: string; label: string; adminOnly?: boolean };

const NAV: NavItem[] = [
  { href: "/", label: "Overview" },
  { href: "/learning", label: "Learning content" },
  { href: "/assessment", label: "Assessment" },
  { href: "/portfolio", label: "Verification" },
  { href: "/accounts", label: "Accounts", adminOnly: true },
  { href: "/roles", label: "Roles", adminOnly: true },
];

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [username, setUsername] = useState<string>("");
  const [admin, setAdmin] = useState(false);

  useEffect(() => {
    setUsername(getAccount()?.username ?? "");
    setAdmin(isAdmin());
  }, []);

  async function logout() {
    await api.logout();
    router.replace("/login");
  }

  const items = NAV.filter((n) => !n.adminOnly || admin);

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 shrink-0 flex-col border-r border-slate-200/80 bg-white">
        <div className="flex items-center gap-2 px-5 py-4 font-semibold">
          <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-gradient text-sm font-bold text-white shadow-soft">
            IT
          </span>
          Console
        </div>
        <nav className="flex-1 space-y-1 px-3 py-2">
          {items.map((n) => {
            const active = n.href === "/" ? pathname === "/" : pathname.startsWith(n.href);
            return (
              <Link
                key={n.href}
                href={n.href}
                className={`block rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  active ? "bg-brand-50 text-brand-700" : "text-slate-600 hover:bg-slate-100"
                }`}
              >
                {n.label}
              </Link>
            );
          })}
        </nav>
        <div className="border-t border-slate-200 px-4 py-3 text-sm">
          <p className="truncate font-medium text-slate-700">{username || "—"}</p>
          <p className="text-xs text-slate-400">{admin ? "Administrator" : "Teacher"}</p>
          <button onClick={logout} className="btn-ghost btn-sm mt-2 w-full">
            Log out
          </button>
        </div>
      </aside>
      <main className="flex-1 overflow-x-hidden">
        <div className="mx-auto max-w-5xl px-6 py-8">{children}</div>
      </main>
    </div>
  );
}
