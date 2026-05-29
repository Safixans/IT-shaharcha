"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Nav from "@/components/Nav";
import AdminTabs from "@/components/AdminTabs";
import { canAuthor, isAuthenticated } from "@/lib/auth";

// Gates the admin authoring surface to teachers/admins. The backend enforces the
// real per-resource permissions; this only avoids rendering tools a user can't use.
export default function AdminGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [allowed, setAllowed] = useState<boolean | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    setAllowed(canAuthor());
  }, [router]);

  if (allowed === null) {
    return (
      <>
        <Nav />
        <main className="mx-auto max-w-5xl px-4 py-16 text-slate-500">Loading…</main>
      </>
    );
  }

  if (!allowed) {
    return (
      <>
        <Nav />
        <main className="mx-auto max-w-5xl px-4 py-16">
          <p className="text-red-600">You don&apos;t have permission to access authoring tools.</p>
        </main>
      </>
    );
  }

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-5xl space-y-6 px-4 py-10">
        <AdminTabs />
        {children}
      </main>
    </>
  );
}
