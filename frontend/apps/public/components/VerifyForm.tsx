"use client";

import { useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { api, ApiError } from "@itsh/api-client";
import { AuthShell } from "./AuthShell";

export function VerifyForm() {
  const router = useRouter();
  const params = useSearchParams();
  const [email, setEmail] = useState(params.get("email") ?? "");
  const [code, setCode] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.verify({ email, code });
      router.push("/login");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Verification failed.");
      setBusy(false);
    }
  }

  async function resend() {
    setError(null);
    setNotice(null);
    try {
      await api.resendOtp({ email });
      setNotice("If the account exists, a new code has been sent.");
    } catch {
      setNotice("If the account exists, a new code has been sent.");
    }
  }

  return (
    <AuthShell title="Verify your email" subtitle="Enter the code we sent to your inbox.">
      <form onSubmit={onSubmit} className="space-y-4">
        <div>
          <label className="label" htmlFor="email">
            Email
          </label>
          <input
            id="email"
            type="email"
            className="input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="label" htmlFor="code">
            Verification code
          </label>
          <input
            id="code"
            className="input"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            inputMode="numeric"
            required
          />
        </div>
        {error && (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        )}
        {notice && <p className="text-sm text-emerald-600">{notice}</p>}
        <button type="submit" className="btn-primary w-full" disabled={busy}>
          {busy ? "Verifying…" : "Verify"}
        </button>
        <button type="button" className="btn-ghost w-full" onClick={resend}>
          Resend code
        </button>
      </form>
    </AuthShell>
  );
}
