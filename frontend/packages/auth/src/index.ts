// Minimal token store backed by localStorage. Client-side only.
// Shared across all frontend apps (public, learner, console).

export type Account = {
  id: string;
  email: string;
  username: string;
  status: string;
  emailVerified: boolean;
  provider: string | null;
  roles: string[];
};

const ACCESS_KEY = "itsh.accessToken";
const REFRESH_KEY = "itsh.refreshToken";
const ACCOUNT_KEY = "itsh.account";

export function saveSession(
  accessToken: string,
  refreshToken: string,
  account: Account,
) {
  localStorage.setItem(ACCESS_KEY, accessToken);
  localStorage.setItem(REFRESH_KEY, refreshToken);
  localStorage.setItem(ACCOUNT_KEY, JSON.stringify(account));
}

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(REFRESH_KEY);
}

export function setAccessToken(token: string) {
  localStorage.setItem(ACCESS_KEY, token);
}

export function setRefreshToken(token: string) {
  localStorage.setItem(REFRESH_KEY, token);
}

export function getAccount(): Account | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(ACCOUNT_KEY);
  return raw ? (JSON.parse(raw) as Account) : null;
}

export function clearSession() {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(ACCOUNT_KEY);
}

export function isAuthenticated(): boolean {
  return !!getAccessToken();
}

export function hasRole(role: string): boolean {
  return getAccount()?.roles?.includes(role) ?? false;
}

// Authoring (create/edit) is open to teachers and admins; the backend enforces
// the precise per-resource permission, this only gates UI affordances.
export function canAuthor(): boolean {
  return hasRole("ROLE_TEACHER") || hasRole("ROLE_ADMIN");
}

export function isAdmin(): boolean {
  return hasRole("ROLE_ADMIN");
}
