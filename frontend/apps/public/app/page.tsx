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
    title: "Learn IT, free",
    body: "Structured tracks, courses, tutorials, docs, and typing practice — progress tracked end to end.",
    href: "/register",
  },
  {
    title: "Prep for IELTS & SAT",
    body: "Mock exams with real timing and synchronous scoring, plus practice that builds toward the real thing.",
    href: "/register",
  },
  {
    title: "Build a portfolio",
    body: "Collect verified certificates, education, and projects — then publish a shareable academic profile.",
    href: "/register",
  },
];

export default async function LandingPage() {
  const tracks = await loadTracks();

  return (
    <>
      <SiteHeader />
      <main className="flex-1">
        <section className="mx-auto max-w-6xl px-4 py-20 text-center">
          <h1 className="mx-auto max-w-3xl text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
            Learn, get assessed, and prove it — all in one place.
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-slate-600">
            IT-Shaharcha is a free education platform: master IT skills, prepare for IELTS and
            SAT, and build a verifiable academic portfolio that you can share with anyone.
          </p>
          <div className="mt-8 flex items-center justify-center gap-3">
            <Link href="/register" className="btn-primary px-6 py-3 text-base">
              Get started — it&apos;s free
            </Link>
            <Link href="/rankings" className="btn-ghost px-6 py-3 text-base">
              View rankings
            </Link>
          </div>
        </section>

        <section className="mx-auto max-w-6xl px-4 pb-16">
          <div className="grid gap-5 sm:grid-cols-3">
            {FEATURES.map((f) => (
              <Link key={f.title} href={f.href} className="card transition-shadow hover:shadow-md">
                <h2 className="text-lg font-semibold text-slate-900">{f.title}</h2>
                <p className="mt-2 text-sm text-slate-600">{f.body}</p>
              </Link>
            ))}
          </div>
        </section>

        {tracks.length > 0 && (
          <section className="mx-auto max-w-6xl px-4 pb-20">
            <h2 className="mb-4 text-xl font-semibold text-slate-900">Explore tracks</h2>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {tracks.map((t) => (
                <div key={t.id} className="card">
                  <h3 className="font-semibold text-slate-900">{t.title}</h3>
                  {t.description && (
                    <p className="mt-1 line-clamp-2 text-sm text-slate-600">{t.description}</p>
                  )}
                  <p className="mt-3 text-xs font-medium text-slate-400">
                    {t.courseCount} course{t.courseCount === 1 ? "" : "s"}
                  </p>
                </div>
              ))}
            </div>
          </section>
        )}
      </main>
      <SiteFooter />
    </>
  );
}
