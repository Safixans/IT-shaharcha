import type { Metadata } from "next";
import Link from "next/link";
import { SiteHeader } from "../../components/SiteHeader";
import { SiteFooter } from "../../components/SiteFooter";
import { TypingArena } from "../../components/TypingArena";

export const metadata: Metadata = {
  title: "Typing practice",
  description: "A free, no-login typing speed test. Measure your WPM and accuracy in seconds.",
};

export default function TypingPage() {
  return (
    <>
      <SiteHeader />
      <main className="flex-1">
        <section className="mx-auto max-w-4xl px-4 py-14">
          <div className="mb-8 text-center">
            <span className="badge-brand mx-auto mb-4">No sign-up required</span>
            <h1 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">
              Typing <span className="bg-brand-gradient bg-clip-text text-transparent">speed test</span>
            </h1>
            <p className="mx-auto mt-3 max-w-xl text-slate-600">
              Pick a duration and start typing — your words-per-minute and accuracy update live. Nothing to install,
              no account needed.
            </p>
          </div>

          <TypingArena />

          <div className="mt-12 grid gap-4 sm:grid-cols-3">
            <Tip title="Stay accurate" body="Speed follows accuracy. Slow down enough to hit the right keys first." />
            <Tip title="Don't look down" body="Trust your fingers. Looking at the keyboard caps your top speed." />
            <Tip title="Practice daily" body="Short, frequent sessions beat occasional long ones for building muscle memory." />
          </div>

          <div className="mt-12 rounded-2xl bg-brand-gradient p-8 text-center text-white shadow-lift">
            <h2 className="text-xl font-semibold">Want to track your progress?</h2>
            <p className="mx-auto mt-1 max-w-md text-sm text-white/85">
              Create a free account to save your typing history, climb the rankings, and unlock IT courses, exams, and
              a verifiable portfolio.
            </p>
            <Link href="/register" className="btn mt-5 bg-white text-brand-700 hover:bg-white/90">
              Create a free account
            </Link>
          </div>
        </section>
      </main>
      <SiteFooter />
    </>
  );
}

function Tip({ title, body }: { title: string; body: string }) {
  return (
    <div className="card">
      <p className="font-semibold text-slate-900">{title}</p>
      <p className="mt-1 text-sm text-slate-600">{body}</p>
    </div>
  );
}
