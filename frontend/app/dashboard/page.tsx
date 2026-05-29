"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Nav from "@/components/Nav";
import { api, ApiError, Profile, ProfileLink } from "@/lib/api";
import { Account, getAccount, isAuthenticated } from "@/lib/auth";

export default function DashboardPage() {
  const router = useRouter();
  const [account, setAccount] = useState<Account | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    setAccount(getAccount());
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const p = await api.myProfile();
      setProfile(p);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        router.replace("/login");
        return;
      }
      setError(err instanceof ApiError ? err.message : "Failed to load profile.");
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <>
        <Nav />
        <main className="mx-auto max-w-5xl px-4 py-16 text-slate-500">Loading…</main>
      </>
    );
  }

  return (
    <>
      <Nav />
      <main className="mx-auto max-w-3xl space-y-6 px-4 py-10">
        <div>
          <h1 className="text-2xl font-bold">Dashboard</h1>
          {account && (
            <p className="mt-1 text-sm text-slate-600">
              Signed in as <span className="font-medium">{account.username}</span> ·{" "}
              {account.email} · roles: {account.roles.join(", ")}
            </p>
          )}
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        {profile && <ProfileCard profile={profile} onSaved={setProfile} />}
      </main>
    </>
  );
}

function ProfileCard({
  profile,
  onSaved,
}: {
  profile: Profile;
  onSaved: (p: Profile) => void;
}) {
  const [fullName, setFullName] = useState(profile.fullName ?? "");
  const [bio, setBio] = useState(profile.bio ?? "");
  const [country, setCountry] = useState(profile.country ?? "");
  const [locale, setLocale] = useState(profile.locale ?? "");
  const [avatarUrl, setAvatarUrl] = useState(profile.avatarUrl ?? "");
  const [links, setLinks] = useState<ProfileLink[]>(profile.links ?? []);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  function updateLink(index: number, field: keyof ProfileLink, value: string) {
    setLinks((prev) => prev.map((l, i) => (i === index ? { ...l, [field]: value } : l)));
  }

  function addLink() {
    setLinks((prev) => [...prev, { label: "", url: "" }]);
  }

  function removeLink(index: number) {
    setLinks((prev) => prev.filter((_, i) => i !== index));
  }

  async function save(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setMsg(null);
    try {
      const cleanLinks = links.filter((l) => l.label.trim() && l.url.trim());
      const updated = await api.updateProfile({
        fullName,
        bio,
        country,
        locale,
        avatarUrl,
        links: cleanLinks,
      });
      onSaved(updated);
      setLinks(updated.links ?? []);
      setMsg("Saved.");
    } catch (err) {
      setMsg(err instanceof ApiError ? err.message : "Failed to save.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={save} className="card space-y-4">
      <h2 className="text-lg font-semibold">Profile</h2>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <label className="label">Full name</label>
          <input className="input" value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </div>
        <div>
          <label className="label">Country</label>
          <input className="input" value={country} onChange={(e) => setCountry(e.target.value)} />
        </div>
        <div>
          <label className="label">Locale</label>
          <input className="input" value={locale} onChange={(e) => setLocale(e.target.value)} />
        </div>
        <div>
          <label className="label">Avatar URL</label>
          <input className="input" value={avatarUrl} onChange={(e) => setAvatarUrl(e.target.value)} />
        </div>
      </div>
      <div>
        <label className="label">Bio</label>
        <textarea
          className="input min-h-20"
          value={bio}
          onChange={(e) => setBio(e.target.value)}
        />
      </div>

      <div className="space-y-2 border-t border-slate-100 pt-3">
        <div className="flex items-center justify-between">
          <label className="label">Links</label>
          <button type="button" className="btn-ghost text-sm" onClick={addLink}>
            Add link
          </button>
        </div>
        {links.length === 0 && <p className="text-sm text-slate-400">No links yet.</p>}
        {links.map((link, i) => (
          <div key={i} className="flex gap-2">
            <input
              className="input"
              placeholder="Label"
              value={link.label}
              onChange={(e) => updateLink(i, "label", e.target.value)}
            />
            <input
              className="input"
              placeholder="https://…"
              value={link.url}
              onChange={(e) => updateLink(i, "url", e.target.value)}
            />
            <button
              type="button"
              className="btn-ghost px-3"
              onClick={() => removeLink(i)}
              aria-label="Remove link"
            >
              ✕
            </button>
          </div>
        ))}
      </div>

      <div className="flex items-center gap-3">
        <button className="btn-primary" disabled={saving}>
          {saving ? "Saving…" : "Save profile"}
        </button>
        {msg && <span className="text-sm text-slate-500">{msg}</span>}
      </div>
    </form>
  );
}
