"use client";

import { useEffect, useRef, useState } from "react";
import {
  api,
  ApiError,
  type ItemKind,
  type Portfolio,
} from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../components/ui";
import { PUBLIC_URL } from "../../../lib/links";

const ITEM_KINDS: ItemKind[] = ["project", "award", "publication", "experience", "skill", "link"];

export default function PortfolioPage() {
  const [pf, setPf] = useState<Portfolio | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    try {
      setPf(await api.getMyPortfolio());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load portfolio.");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  function fail(err: unknown) {
    setError(err instanceof ApiError ? err.message : "Action failed.");
  }

  if (pf === null && !error) return <Loading />;

  return (
    <>
      <PageHeader title="My portfolio" description="Certificates, education, projects — then publish." />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      {pf && (
        <div className="space-y-8">
          <PublishCard portfolio={pf} onChange={load} onError={fail} />
          <Certificates portfolio={pf} onChange={load} onError={fail} />
          <EducationSection portfolio={pf} onChange={load} onError={fail} />
          <Items portfolio={pf} onChange={load} onError={fail} />
        </div>
      )}
    </>
  );
}

function PublishCard({ portfolio, onChange, onError }: SectionProps) {
  const [handle, setHandle] = useState(portfolio.handle ?? "");
  const [visibility, setVisibility] = useState<"public" | "unlisted">(
    portfolio.visibility === "unlisted" ? "unlisted" : "public",
  );
  const [busy, setBusy] = useState(false);

  async function publish() {
    setBusy(true);
    try {
      await api.publishPortfolio({ handle: handle || undefined, visibility });
      onChange();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  const published = portfolio.publishedAt != null;

  return (
    <section className="card">
      <h2 className="font-semibold text-slate-900">Publish</h2>
      <p className="mt-1 text-sm text-slate-600">
        {published ? "Your portfolio is live." : "Publish to share a public link."}
      </p>
      <div className="mt-4 grid gap-3 sm:grid-cols-[1fr_10rem_auto] sm:items-end">
        <Field label="Public handle">
          <input className="input" value={handle} onChange={(e) => setHandle(e.target.value)} placeholder="your-handle" />
        </Field>
        <Field label="Visibility">
          <select className="select" value={visibility} onChange={(e) => setVisibility(e.target.value as "public" | "unlisted")}>
            <option value="public">public</option>
            <option value="unlisted">unlisted</option>
          </select>
        </Field>
        <button className="btn-primary" onClick={publish} disabled={busy}>
          {busy ? "Publishing…" : published ? "Update" : "Publish"}
        </button>
      </div>
      {published && portfolio.handle && (
        <a
          href={`${PUBLIC_URL}/p/${portfolio.handle}`}
          target="_blank"
          rel="noopener noreferrer"
          className="mt-3 inline-block text-sm font-medium text-brand-600 hover:text-brand-700"
        >
          View public page → {PUBLIC_URL}/p/{portfolio.handle}
        </a>
      )}
    </section>
  );
}

function Certificates({ portfolio, onChange, onError }: SectionProps) {
  const [title, setTitle] = useState("");
  const [issuer, setIssuer] = useState("");
  const fileRef = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);

  async function add(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    try {
      let fileId: string | undefined;
      const file = fileRef.current?.files?.[0];
      if (file) fileId = (await api.uploadFile(file)).fileId;
      await api.createCertificate({ title, issuer: issuer || undefined, fileId });
      setTitle("");
      setIssuer("");
      if (fileRef.current) fileRef.current.value = "";
      onChange();
    } catch (err) {
      onError(err);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section>
      <h2 className="mb-3 font-semibold text-slate-900">Certificates</h2>
      <div className="grid gap-4 sm:grid-cols-[1fr_18rem]">
        <List
          empty="No certificates yet."
          rows={portfolio.certificates.map((c) => ({
            id: c.id,
            title: c.title,
            subtitle: c.issuer ?? undefined,
            badge: c.status,
            onDelete: () => api.deleteCertificate(c.id).then(onChange).catch(onError),
          }))}
        />
        <form onSubmit={add} className="card h-fit space-y-3">
          <h3 className="font-semibold text-slate-900">Add certificate</h3>
          <Field label="Title">
            <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
          </Field>
          <Field label="Issuer">
            <input className="input" value={issuer} onChange={(e) => setIssuer(e.target.value)} />
          </Field>
          <Field label="File (PDF/image)">
            <input ref={fileRef} type="file" className="text-sm" accept="application/pdf,image/*" />
          </Field>
          <button className="btn-primary w-full" disabled={busy}>
            {busy ? "Uploading…" : "Add certificate"}
          </button>
        </form>
      </div>
    </section>
  );
}

function EducationSection({ portfolio, onChange, onError }: SectionProps) {
  const [institution, setInstitution] = useState("");
  const [degree, setDegree] = useState("");
  const [field, setField] = useState("");

  async function add(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.addEducation({ institution, degree: degree || undefined, fieldOfStudy: field || undefined });
      setInstitution("");
      setDegree("");
      setField("");
      onChange();
    } catch (err) {
      onError(err);
    }
  }

  return (
    <section>
      <h2 className="mb-3 font-semibold text-slate-900">Education</h2>
      <div className="grid gap-4 sm:grid-cols-[1fr_18rem]">
        <List
          empty="No education entries yet."
          rows={portfolio.education.map((ed) => ({
            id: ed.id,
            title: ed.institution,
            subtitle: [ed.degree, ed.fieldOfStudy].filter(Boolean).join(" · ") || undefined,
            onDelete: () => api.deleteEducation(ed.id).then(onChange).catch(onError),
          }))}
        />
        <form onSubmit={add} className="card h-fit space-y-3">
          <h3 className="font-semibold text-slate-900">Add education</h3>
          <Field label="Institution">
            <input className="input" value={institution} onChange={(e) => setInstitution(e.target.value)} required />
          </Field>
          <Field label="Degree">
            <input className="input" value={degree} onChange={(e) => setDegree(e.target.value)} />
          </Field>
          <Field label="Field of study">
            <input className="input" value={field} onChange={(e) => setField(e.target.value)} />
          </Field>
          <button className="btn-primary w-full">Add education</button>
        </form>
      </div>
    </section>
  );
}

function Items({ portfolio, onChange, onError }: SectionProps) {
  const [kind, setKind] = useState<ItemKind>("project");
  const [title, setTitle] = useState("");
  const [url, setUrl] = useState("");

  async function add(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.addItem({ kind, title, url: url || undefined });
      setTitle("");
      setUrl("");
      onChange();
    } catch (err) {
      onError(err);
    }
  }

  return (
    <section>
      <h2 className="mb-3 font-semibold text-slate-900">Projects &amp; more</h2>
      <div className="grid gap-4 sm:grid-cols-[1fr_18rem]">
        <List
          empty="No items yet."
          rows={portfolio.items.map((i) => ({
            id: i.id,
            title: i.title,
            subtitle: i.kind,
            onDelete: () => api.deleteItem(i.id).then(onChange).catch(onError),
          }))}
        />
        <form onSubmit={add} className="card h-fit space-y-3">
          <h3 className="font-semibold text-slate-900">Add item</h3>
          <Field label="Kind">
            <select className="select" value={kind} onChange={(e) => setKind(e.target.value as ItemKind)}>
              {ITEM_KINDS.map((k) => (
                <option key={k} value={k}>
                  {k}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Title">
            <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
          </Field>
          <Field label="URL">
            <input className="input" value={url} onChange={(e) => setUrl(e.target.value)} />
          </Field>
          <button className="btn-primary w-full">Add item</button>
        </form>
      </div>
    </section>
  );
}

type SectionProps = { portfolio: Portfolio; onChange: () => void; onError: (e: unknown) => void };

type RowSpec = { id: string; title: string; subtitle?: string; badge?: string; onDelete: () => void };

function List({ rows, empty }: { rows: RowSpec[]; empty: string }) {
  if (rows.length === 0) return <div className="card text-sm text-slate-400">{empty}</div>;
  return (
    <ul className="overflow-hidden rounded-xl border border-slate-200 bg-white">
      {rows.map((r) => (
        <li key={r.id} className="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
          <div className="min-w-0">
            <p className="truncate font-medium text-slate-900">{r.title}</p>
            {r.subtitle && <p className="truncate text-xs text-slate-500">{r.subtitle}</p>}
          </div>
          <div className="flex shrink-0 items-center gap-2">
            {r.badge && <span className="badge bg-slate-100 text-slate-600">{r.badge.toLowerCase()}</span>}
            <button className="btn-danger btn-sm" onClick={r.onDelete}>
              Delete
            </button>
          </div>
        </li>
      ))}
    </ul>
  );
}
