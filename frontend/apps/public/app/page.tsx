import Link from "next/link";
import { api, type Track } from "@itsh/api-client";
import { SiteHeader } from "../components/SiteHeader";
import { SiteFooter } from "../components/SiteFooter";

// Featured tracks are public; fetch on the server for SEO. If the backend is
// unavailable (e.g. during a static build), fall back to an empty list.
async function loadTracks(): Promise<Track[]> {
  try {
    const page = await api.serverListTracks({ size: 6 });
    return page.items;
  } catch {
    return [];
  }
}

const FEATURES = [
  {
    icon: "📚",
    title: "Learn IT, free",
    body: "Structured tracks, courses, tutorials, docs, and typing practice — progress tracked end to end.",
    href: "/register",
  },
  {
    icon: "📝",
    title: "Prep for IELTS & SAT",
    body: "Mock exams with real timing and synchronous scoring, plus practice that builds toward the real thing.",
    href: "/register",
  },
  {
    icon: "🎓",
    title: "Build a portfolio",
    body: "Collect verified certificates, education, and projects — then publish a shareable academic profile.",
    href: "/register",
  },
  {
    icon: "⌨️",
    title: "Test your typing",
    body: "A free, no-login speed test with live WPM and accuracy. Warm up before you start coding.",
    href: "/typing",
  },
];

const STATS = [
  { value: "100%", label: "Free, forever" },
  { value: "4", label: "Learning roadmaps" },
  { value: "IELTS · SAT", label: "Exam prep" },
  { value: "Verified", label: "Portfolios" },
];

const STEPS = [
  {
    n: "01",
    title: "Learn",
    body: "Follow a roadmap from zero to job-ready with courses, tutorials, and hands-on practice.",
  },
  {
    n: "02",
    title: "Get assessed",
    body: "Take timed mock exams and practice sets that score instantly, so you always know where you stand.",
  },
  {
    n: "03",
    title: "Prove it",
    body: "Earn verified certificates and publish a shareable academic portfolio employers can trust.",
  },
];

export default async function LandingPage() {
  const tracks = await loadTracks();

  return (
    <>
      <SiteHeader />
      <main className="flex-1">
        {/* Hero */}
        <section className="relative overflow-hidden bg-hero-grid">
          <div className="pointer-events-none absolute inset-0 bg-hatch opacity-60" />
          <div className="pointer-events-none absolute -right-24 -top-24 h-72 w-72 rounded-full bg-brand-gradient opacity-20 blur-3xl" />
          <div className="pointer-events-none absolute -bottom-32 -left-24 h-72 w-72 rounded-full bg-brand-gradient opacity-10 blur-3xl" />
          <div className="relative mx-auto max-w-6xl px-4 py-24 text-center sm:py-28">
            <span className="badge-brand mx-auto mb-6 inline-flex">
              <span className="h-1.5 w-1.5 rounded-full bg-brand-500" />
              Free education platform
            </span>
            <h1 className="mx-auto max-w-3xl text-4xl font-extrabold tracking-tight text-ink sm:text-6xl sm:leading-[1.05]">
              Learn, get assessed, and{" "}
              <span className="text-gradient">prove it</span> — all in one place.
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-slate-600">
              IT-Shaharcha is a free education platform: master IT skills, prepare for IELTS and
              SAT, and build a verifiable academic portfolio that you can share with anyone.
            </p>
            <div className="mt-9 flex flex-col items-center justify-center gap-3 sm:flex-row">
              <Link href="/register" className="btn-gradient btn-lg w-full sm:w-auto">
                Get started — it&apos;s free
              </Link>
              <Link href="/typing" className="btn-ghost btn-lg w-full sm:w-auto">
                Try the typing test
              </Link>
            </div>

            <dl className="mx-auto mt-16 grid max-w-4xl grid-cols-2 gap-px overflow-hidden rounded-3xl border border-slate-200/70 bg-slate-200/60 shadow-soft sm:grid-cols-4">
              {STATS.map((s) => (
                <div key={s.label} className="bg-white px-4 py-6">
                  <dt className="text-2xl font-extrabold text-gradient">{s.value}</dt>
                  <dd className="mt-1 text-xs font-medium uppercase tracking-wide text-slate-500">
                    {s.label}
                  </dd>
                </div>
              ))}
            </dl>
          </div>
        </section>

        {/* Features */}
        <section id="features" className="mx-auto max-w-6xl px-4 py-20">
          <div className="mx-auto mb-12 max-w-2xl text-center">
            <p className="section-eyebrow">Everything you need</p>
            <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">
              One platform, the whole journey
            </h2>
          </div>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {FEATURES.map((f) => (
              <Link key={f.title} href={f.href} className="card-hover group">
                <div className="mb-4 grid h-12 w-12 place-items-center rounded-2xl bg-brand-gradient text-2xl shadow-lift transition-transform group-hover:scale-105">
                  {f.icon}
                </div>
                <h3 className="text-lg font-semibold text-ink">{f.title}</h3>
                <p className="mt-2 text-sm text-slate-600">{f.body}</p>
              </Link>
            ))}
          </div>
        </section>

        {/* How it works */}
        <section id="about" className="bg-white py-20">
          <div className="mx-auto max-w-6xl px-4">
            <div className="mx-auto mb-12 max-w-2xl text-center">
              <p className="section-eyebrow">How it works</p>
              <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">
                From first lesson to verified proof
              </h2>
            </div>
            <div className="grid gap-6 md:grid-cols-3">
              {STEPS.map((s) => (
                <div key={s.n} className="card-feature">
                  <span className="text-5xl font-extrabold text-gradient opacity-90">{s.n}</span>
                  <h3 className="mt-4 text-xl font-semibold text-ink">{s.title}</h3>
                  <p className="mt-2 text-sm text-slate-600">{s.body}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Tracks */}
        {tracks.length > 0 && (
          <section className="mx-auto max-w-6xl px-4 py-20">
            <div className="mb-8 flex items-end justify-between">
              <div>
                <p className="section-eyebrow">Start learning</p>
                <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">Explore tracks</h2>
              </div>
              <Link href="/register" className="text-sm font-semibold text-brand-600 hover:text-brand-700">
                See all →
              </Link>
            </div>
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {tracks.map((t) => (
                <Link key={t.id} href="/register" className="card-hover">
                  <h3 className="font-semibold text-ink">{t.title}</h3>
                  {t.description && (
                    <p className="mt-1 line-clamp-2 text-sm text-slate-600">{t.description}</p>
                  )}
                  <p className="mt-4 badge-slate">
                    {t.courseCount} course{t.courseCount === 1 ? "" : "s"}
                  </p>
                </Link>
              ))}
            </div>
          </section>
        )}

        {/* CTA */}
        <section className="mx-auto max-w-6xl px-4 pb-24">
          <div className="relative overflow-hidden rounded-[2.5rem] bg-ink-gradient px-6 py-16 text-center shadow-lift sm:px-12">
            <div className="pointer-events-none absolute inset-0 bg-hatch opacity-30" />
            <div className="pointer-events-none absolute -right-20 -top-20 h-64 w-64 rounded-full bg-brand-gradient opacity-30 blur-3xl" />
            <div className="relative">
              <h2 className="mx-auto max-w-2xl text-3xl font-bold tracking-tight text-white sm:text-4xl">
                Ready to start learning for free?
              </h2>
              <p className="mx-auto mt-4 max-w-xl text-base text-slate-300">
                Join IT-Shaharcha and turn your effort into skills, scores, and a portfolio you own.
              </p>
              <Link href="/register" className="btn-gradient btn-lg mt-8 inline-flex">
                Create your free account
              </Link>
            </div>
          </div>
        </section>
      </main>
      <SiteFooter />
    </>
  );
}
