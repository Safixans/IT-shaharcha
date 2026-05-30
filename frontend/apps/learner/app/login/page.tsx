"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api, ApiError } from "@itsh/api-client";

export default function LearnerLogin() {
  const router = useRouter();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.login({ identifier, password });
      router.replace("/");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Login failed. Try again.");
      setBusy(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-4">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex items-center justify-center gap-2 font-semibold">
          <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-500 text-sm font-bold text-white">
            IT
          </span>
          IT-Shaharcha
        </div>
        <form onSubmit={onSubmit} className="card space-y-4">
          <h1 className="text-lg font-semibold text-slate-900">Welcome back</h1>
          <div>
            <label className="label" htmlFor="identifier">Email or username</label>
            <input id="identifier" className="input" value={identifier} onChange={(e) => setIdentifier(e.target.value)} autoComplete="username" required />
          </div>
          <div>
            <label className="label" htmlFor="password">Password</label>
            <input id="password" type="password" className="input" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required />
          </div>
          {error && <p role="alert" className="text-sm text-red-600">{error}</p>}
          <button type="submit" className="btn-primary w-full" disabled={busy}>
            {busy ? "Logging in…" : "Log in"}
          </button>
        </form>
      </div>
    </main>
  );
}
