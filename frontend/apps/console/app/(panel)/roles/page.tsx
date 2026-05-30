"use client";

import { useEffect, useState } from "react";
import { api, ApiError, type Role } from "@itsh/api-client";
import { ErrorBanner, Field, Loading, PageHeader } from "../../../components/ui";

export default function RolesPage() {
  const [roles, setRoles] = useState<Role[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [busy, setBusy] = useState(false);

  async function load() {
    setError(null);
    try {
      setRoles(await api.listRoles());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load roles.");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function create(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const role = name.startsWith("ROLE_") ? name : `ROLE_${name.toUpperCase()}`;
      await api.createRole({ name: role, description: description || undefined });
      setName("");
      setDescription("");
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create role.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(roleName: string) {
    setError(null);
    try {
      await api.deleteRole(roleName);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not delete role.");
    }
  }

  return (
    <>
      <PageHeader title="Roles" description="The platform role catalog." />

      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      <div className="grid gap-6 sm:grid-cols-[1fr_18rem]">
        <div>
          {roles === null ? (
            <Loading />
          ) : roles.length === 0 ? (
            <div className="card text-sm text-slate-400">No roles defined.</div>
          ) : (
            <ul className="overflow-hidden rounded-xl border border-slate-200 bg-white">
              {roles.map((r) => (
                <li key={r.name} className="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
                  <div>
                    <p className="font-medium text-slate-900">{r.name}</p>
                    {r.description && <p className="text-xs text-slate-500">{r.description}</p>}
                  </div>
                  <button className="btn-danger btn-sm" onClick={() => remove(r.name)}>
                    Delete
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <form onSubmit={create} className="card h-fit space-y-3">
          <h2 className="font-semibold text-slate-900">New role</h2>
          <Field label="Name" hint="ROLE_ prefix added automatically.">
            <input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="MODERATOR" required />
          </Field>
          <Field label="Description">
            <input className="input" value={description} onChange={(e) => setDescription(e.target.value)} />
          </Field>
          <button className="btn-primary w-full" disabled={busy}>
            {busy ? "Creating…" : "Create role"}
          </button>
        </form>
      </div>
    </>
  );
}
