"use client";

import { useCallback, useEffect, useState } from "react";
import {
  api,
  ApiError,
  type Account,
  type AccountStatus,
  type Page,
  type Role,
} from "@itsh/api-client";
import { ErrorBanner, Loading, PageHeader } from "../../../components/ui";

const STATUS_STYLES: Record<string, string> = {
  ACTIVE: "bg-emerald-100 text-emerald-700",
  SUSPENDED: "bg-red-100 text-red-700",
  PENDING: "bg-amber-100 text-amber-700",
  DEACTIVATED: "bg-slate-200 text-slate-600",
};

export default function AccountsPage() {
  const [data, setData] = useState<Page<Account> | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [status, setStatus] = useState<AccountStatus | "">("");
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState<string | null>(null);
  const [draftRoles, setDraftRoles] = useState<Set<string>>(new Set());

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await api.listAccounts({ status: status || undefined, q: q || undefined, size: 50 });
      setData(page);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load accounts.");
    } finally {
      setLoading(false);
    }
  }, [status, q]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    api.listRoles().then(setRoles).catch(() => setRoles([]));
  }, []);

  async function act(fn: () => Promise<Account>) {
    try {
      const updated = await fn();
      setData((d) =>
        d ? { ...d, items: d.items.map((a) => (a.id === updated.id ? updated : a)) } : d,
      );
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Action failed.");
    }
  }

  function startEdit(a: Account) {
    setEditing(a.id);
    setDraftRoles(new Set(a.roles));
  }

  async function saveRoles(accountId: string) {
    await act(() => api.setAccountRoles(accountId, [...draftRoles]));
    setEditing(null);
  }

  return (
    <>
      <PageHeader title="Accounts" description="Manage platform accounts and their roles." />

      <div className="mb-4 flex flex-wrap gap-2">
        <input
          className="input max-w-xs"
          placeholder="Search email or username…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && load()}
        />
        <select className="select max-w-[10rem]" value={status} onChange={(e) => setStatus(e.target.value as AccountStatus | "")}>
          <option value="">All statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="SUSPENDED">Suspended</option>
          <option value="PENDING">Pending</option>
          <option value="DEACTIVATED">Deactivated</option>
        </select>
        <button className="btn-ghost" onClick={load}>
          Apply
        </button>
      </div>

      {error && <div className="mb-4"><ErrorBanner message={error} /></div>}

      {loading ? (
        <Loading />
      ) : !data || data.items.length === 0 ? (
        <div className="card text-sm text-slate-400">No accounts found.</div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-400">
              <tr>
                <th className="px-4 py-2 font-medium">Account</th>
                <th className="px-4 py-2 font-medium">Status</th>
                <th className="px-4 py-2 font-medium">Roles</th>
                <th className="px-4 py-2 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((a) => (
                <tr key={a.id} className="border-t border-slate-100 align-top">
                  <td className="px-4 py-3">
                    <p className="font-medium text-slate-900">{a.username}</p>
                    <p className="text-xs text-slate-500">{a.email}</p>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`badge ${STATUS_STYLES[a.status] ?? "bg-slate-100 text-slate-600"}`}>
                      {a.status.toLowerCase()}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {editing === a.id ? (
                      <div className="space-y-1">
                        {roles.map((r) => (
                          <label key={r.name} className="flex items-center gap-2 text-xs">
                            <input
                              type="checkbox"
                              checked={draftRoles.has(r.name)}
                              onChange={(e) => {
                                const next = new Set(draftRoles);
                                if (e.target.checked) next.add(r.name);
                                else next.delete(r.name);
                                setDraftRoles(next);
                              }}
                            />
                            {r.name}
                          </label>
                        ))}
                        <div className="flex gap-2 pt-1">
                          <button className="btn-primary btn-sm" onClick={() => saveRoles(a.id)}>
                            Save
                          </button>
                          <button className="btn-ghost btn-sm" onClick={() => setEditing(null)}>
                            Cancel
                          </button>
                        </div>
                      </div>
                    ) : (
                      <div className="flex flex-wrap gap-1">
                        {a.roles.length === 0 ? (
                          <span className="text-xs text-slate-400">none</span>
                        ) : (
                          a.roles.map((r) => (
                            <span key={r} className="badge bg-slate-100 text-slate-600">
                              {r.replace("ROLE_", "")}
                            </span>
                          ))
                        )}
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      {a.status === "SUSPENDED" ? (
                        <button className="btn-ghost btn-sm" onClick={() => act(() => api.activateAccount(a.id))}>
                          Activate
                        </button>
                      ) : (
                        <button className="btn-danger btn-sm" onClick={() => act(() => api.suspendAccount(a.id))}>
                          Suspend
                        </button>
                      )}
                      <button className="btn-ghost btn-sm" onClick={() => startEdit(a)}>
                        Roles
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
