"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getAccount, isAdmin } from "@itsh/auth";

type Card = { href: string; title: string; body: string; adminOnly?: boolean };

const CARDS: Card[] = [
  { href: "/learning", title: "Learning content", body: "Author tracks, courses, modules, lessons, tutorials, docs and typing drills." },
  { href: "/assessment", title: "Assessments", body: "Create exams and build them out with sections and questions." },
  { href: "/portfolio", title: "Verification", body: "Review and verify learner certificates." },
  { href: "/accounts", title: "Accounts", body: "Suspend, activate, and manage roles for any account.", adminOnly: true },
  { href: "/roles", title: "Roles", body: "Manage the platform role catalog.", adminOnly: true },
];

export default function Overview() {
  const [name, setName] = useState("");
  const [admin, setAdmin] = useState(false);

  useEffect(() => {
    setName(getAccount()?.username ?? "");
    setAdmin(isAdmin());
  }, []);

  const cards = CARDS.filter((c) => !c.adminOnly || admin);

  return (
    <>
      <div className="mb-8 overflow-hidden rounded-2xl bg-brand-gradient p-6 text-white shadow-lift sm:p-8">
        <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">
          Welcome{name ? `, ${name}` : ""} 👋
        </h1>
        <p className="mt-1 max-w-lg text-sm text-white/85">
          Manage content, assessments, verification{admin ? ", and accounts" : ""} from one place.
        </p>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        {cards.map((c) => (
          <Link key={c.href} href={c.href} className="card-hover">
            <h2 className="font-semibold text-slate-900">{c.title}</h2>
            <p className="mt-1 text-sm text-slate-600">{c.body}</p>
          </Link>
        ))}
      </div>
    </>
  );
}
