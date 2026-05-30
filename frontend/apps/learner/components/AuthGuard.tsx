"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isAuthenticated } from "@itsh/auth";
import type { ReactNode } from "react";

/** Any authenticated account may use the learner app; anonymous users go to /login. */
export function AuthGuard({ children }: { children: ReactNode }) {
  const router = useRouter();
  const [ok, setOk] = useState(false);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    setOk(true);
  }, [router]);

  if (!ok) return <p className="p-8 text-sm text-slate-400">Loading…</p>;
  return <>{children}</>;
}
