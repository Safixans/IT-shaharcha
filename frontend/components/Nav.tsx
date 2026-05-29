"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { canAuthor, getAccount, isAuthenticated } from "@/lib/auth";
import { api } from "@/lib/api";

export default function Nav() {
  const router = useRouter();
  const pathname = usePathname();
  const [authed, setAuthed] = useState(false);
  const [author, setAuthor] = useState(false);
  const [username, setUsername] = useState<string | null>(null);

  useEffect(() => {
    setAuthed(isAuthenticated());
    setAuthor(canAuthor());
    setUsername(getAccount()?.username ?? null);
  }, [pathname]);

  async function handleLogout() {
    await api.logout();
    router.push("/login");
  }

  return (
    <header className="border-b border-slate-200 bg-white">
      <nav className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <Link href="/" className="text-lg font-bold text-brand-600">
          IT-Shaharcha
        </Link>
        <div className="flex items-center gap-3">
          <Link href="/learn" className="text-sm text-slate-600 hover:text-slate-900">
            Learn
          </Link>
          {authed ? (
            <>
              {author && (
                <Link href="/admin/learning" className="text-sm text-slate-600 hover:text-slate-900">
                  Admin
                </Link>
              )}
              <Link href="/dashboard" className="text-sm text-slate-600 hover:text-slate-900">
                Dashboard
              </Link>
              <span className="text-sm text-slate-400">@{username}</span>
              <button onClick={handleLogout} className="btn-ghost">
                Log out
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="btn-ghost">
                Log in
              </Link>
              <Link href="/register" className="btn-primary">
                Get started
              </Link>
            </>
          )}
        </div>
      </nav>
    </header>
  );
}
