import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { api, ApiError, type Portfolio } from "@itsh/api-client";
import { SiteHeader } from "../../../components/SiteHeader";
import { SiteFooter } from "../../../components/SiteFooter";

type Params = { handle: string };

async function load(handle: string): Promise<Portfolio | null> {
  try {
    return await api.serverGetPublicPortfolio(handle);
  } catch (err) {
    if (err instanceof ApiError && err.status === 404) return null;
    throw err;
  }
}

export async function generateMetadata({
  params,
}: {
  params: Promise<Params>;
}): Promise<Metadata> {
  const { handle } = await params;
  return {
    title: `@${handle}`,
    description: `${handle}'s academic portfolio on IT-Shaharcha.`,
  };
}

export default async function PublicPortfolioPage({
  params,
}: {
  params: Promise<Params>;
}) {
  const { handle } = await params;
  const portfolio = await load(handle);
  if (!portfolio) notFound();

  const verified = portfolio.certificates.filter((c) => c.status === "VERIFIED");

  return (
    <>
      <SiteHeader />
      <main className="flex-1">
        <div className="mx-auto max-w-4xl px-4 py-10">
          <header className="mb-8">
            <p className="text-sm font-medium text-brand-600">Academic portfolio</p>
            <h1 className="mt-1 text-3xl font-bold text-slate-900">@{handle}</h1>
          </header>

          <Section title={`Certificates (${portfolio.certificates.length})`}>
            {portfolio.certificates.length === 0 ? (
              <Empty>No certificates yet.</Empty>
            ) : (
              <ul className="space-y-3">
                {portfolio.certificates.map((c) => (
                  <li key={c.id} className="card flex items-start justify-between gap-3">
                    <div>
                      <p className="font-medium text-slate-900">{c.title}</p>
                      {c.issuer && <p className="text-sm text-slate-600">{c.issuer}</p>}
                      {c.issuedOn && (
                        <p className="mt-1 text-xs text-slate-400">Issued {c.issuedOn}</p>
                      )}
                    </div>
                    <StatusBadge status={c.status} />
                  </li>
                ))}
              </ul>
            )}
          </Section>

          <Section title={`Education (${portfolio.education.length})`}>
            {portfolio.education.length === 0 ? (
              <Empty>No education entries yet.</Empty>
            ) : (
              <ul className="space-y-3">
                {portfolio.education.map((e) => (
                  <li key={e.id} className="card">
                    <p className="font-medium text-slate-900">{e.institution}</p>
                    <p className="text-sm text-slate-600">
                      {[e.degree, e.fieldOfStudy].filter(Boolean).join(" · ")}
                    </p>
                    {(e.startDate || e.endDate) && (
                      <p className="mt-1 text-xs text-slate-400">
                        {e.startDate ?? "?"} — {e.endDate ?? "present"}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </Section>

          <Section title={`Projects & more (${portfolio.items.length})`}>
            {portfolio.items.length === 0 ? (
              <Empty>No portfolio items yet.</Empty>
            ) : (
              <ul className="grid gap-3 sm:grid-cols-2">
                {portfolio.items.map((i) => (
                  <li key={i.id} className="card">
                    <div className="flex items-center justify-between">
                      <p className="font-medium text-slate-900">{i.title}</p>
                      <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-500">
                        {i.kind}
                      </span>
                    </div>
                    {i.description && (
                      <p className="mt-1 text-sm text-slate-600">{i.description}</p>
                    )}
                    {i.url && (
                      <a
                        href={i.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="mt-2 inline-block text-sm font-medium text-brand-600 hover:text-brand-700"
                      >
                        View →
                      </a>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </Section>

          <p className="mt-10 text-sm text-slate-400">
            {verified.length} verified credential{verified.length === 1 ? "" : "s"}.
          </p>
        </div>
      </main>
      <SiteFooter />
    </>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="mb-8">
      <h2 className="mb-3 text-lg font-semibold text-slate-900">{title}</h2>
      {children}
    </section>
  );
}

function Empty({ children }: { children: React.ReactNode }) {
  return <p className="text-sm text-slate-400">{children}</p>;
}

function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    VERIFIED: "bg-emerald-100 text-emerald-700",
    PENDING: "bg-amber-100 text-amber-700",
    REJECTED: "bg-red-100 text-red-700",
  };
  return (
    <span
      className={`shrink-0 rounded-full px-2 py-0.5 text-xs font-medium ${
        styles[status] ?? "bg-slate-100 text-slate-600"
      }`}
    >
      {status.toLowerCase()}
    </span>
  );
}
