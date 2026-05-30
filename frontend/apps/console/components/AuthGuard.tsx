"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isAuthenticated, canAuthor } from "@itsh/auth";
import type { ReactNode } from "react";

type State = "checking" | "allowed" | "forbidden";

/**
 * Gates the console to authenticated teachers/admins. The backend is the real
 * authority (every endpoint is role-checked); this only keeps non-staff out of
 * the UI and bounces anonymous users to the login screen.
 */
export function AuthGuard({ children }: { children: ReactNode }) {
  const router = useRouter();
  const [state, setState] = useState<State>("checking");

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    setState(canAuthor() ? "allowed" : "forbidden");
  }, [router]);

  if (state === "checking") {
    return <p className="p-8 text-sm text-slate-400">Checking access…</p>;
  }

  if (state === "forbidden") {
    return (
      <div className="grid min-h-screen place-items-center p-8 text-center">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Staff access only</h1>
          <p className="mt-2 text-sm text-slate-600">
            This console is for teachers and administrators.
          </p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
