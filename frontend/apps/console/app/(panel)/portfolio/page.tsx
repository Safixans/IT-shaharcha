"use client";

import { useState } from "react";
import { api, ApiError, type Certificate } from "@itsh/api-client";
import { ErrorBanner, Field, PageHeader } from "../../../components/ui";

export default function VerificationPage() {
  const [certId, setCertId] = useState("");
  const [approved, setApproved] = useState(true);
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<Certificate | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const cert = await api.verifyCertificate(certId.trim(), {
        verified: approved,
        note: note || undefined,
      });
      setResult(cert);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Verification failed.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Certificate verification"
        description="Approve or reject a learner's certificate by its ID."
      />

      <div className="grid gap-6 sm:grid-cols-[20rem_1fr]">
        <form onSubmit={submit} className="card h-fit space-y-3">
          <Field label="Certificate ID">
            <input
              className="input font-mono text-xs"
              value={certId}
              onChange={(e) => setCertId(e.target.value)}
              placeholder="uuid"
              required
            />
          </Field>
          <Field label="Decision">
            <select className="select" value={approved ? "approve" : "reject"} onChange={(e) => setApproved(e.target.value === "approve")}>
              <option value="approve">Approve (VERIFIED)</option>
              <option value="reject">Reject (REJECTED)</option>
            </select>
          </Field>
          <Field label="Note (optional)">
            <input className="input" value={note} onChange={(e) => setNote(e.target.value)} />
          </Field>
          <button className="btn-primary w-full" disabled={busy}>
            {busy ? "Submitting…" : "Submit decision"}
          </button>
        </form>

        <div className="space-y-4">
          {error && <ErrorBanner message={error} />}
          {result && (
            <div className="card">
              <p className="text-sm text-slate-500">Updated certificate</p>
              <p className="mt-1 text-lg font-semibold text-slate-900">{result.title}</p>
              {result.issuer && <p className="text-sm text-slate-600">{result.issuer}</p>}
              <p className="mt-3 text-sm">
                Status:{" "}
                <span
                  className={`badge ${
                    result.status === "VERIFIED"
                      ? "bg-emerald-100 text-emerald-700"
                      : result.status === "REJECTED"
                        ? "bg-red-100 text-red-700"
                        : "bg-amber-100 text-amber-700"
                  }`}
                >
                  {result.status.toLowerCase()}
                </span>
              </p>
              {result.verifiedAt && (
                <p className="mt-1 text-xs text-slate-400">Decided {result.verifiedAt}</p>
              )}
            </div>
          )}
          <p className="text-xs text-slate-400">
            Note: the platform exposes verification by certificate ID; there is no reviewer
            queue endpoint yet, so IDs come from the learner or a future moderation feed.
          </p>
        </div>
      </div>
    </>
  );
}
