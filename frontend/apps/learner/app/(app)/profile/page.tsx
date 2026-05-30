"use client";

import { useEffect, useState } from "react";
import { api, ApiError, type Profile } from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../components/ui";

export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [fullName, setFullName] = useState("");
  const [bio, setBio] = useState("");
  const [country, setCountry] = useState("");

  useEffect(() => {
    api
      .myProfile()
      .then((p) => {
        setProfile(p);
        setFullName(p.fullName ?? "");
        setBio(p.bio ?? "");
        setCountry(p.country ?? "");
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load profile."));
  }, []);

  async function save(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setNotice(null);
    try {
      const updated = await api.updateProfile({
        fullName: fullName || undefined,
        bio: bio || undefined,
        country: country || undefined,
      });
      setProfile(updated);
      setNotice("Profile saved.");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not save.");
    } finally {
      setBusy(false);
    }
  }

  if (profile === null && !error) return <Loading />;

  return (
    <>
      <PageHeader title="Profile" description="Update your public details." />
      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}
      {notice && (
        <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {notice}
        </div>
      )}

      <form onSubmit={save} className="card max-w-lg space-y-4">
        <Field label="Username">
          <input className="input bg-slate-50" value={profile?.username ?? ""} disabled />
        </Field>
        <Field label="Full name">
          <input className="input" value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </Field>
        <Field label="Bio">
          <textarea className="input min-h-24" value={bio} onChange={(e) => setBio(e.target.value)} />
        </Field>
        <Field label="Country">
          <input className="input" value={country} onChange={(e) => setCountry(e.target.value)} />
        </Field>
        <button className="btn-primary" disabled={busy}>
          {busy ? "Saving…" : "Save profile"}
        </button>
      </form>
    </>
  );
}
