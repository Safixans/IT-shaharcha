// Anonymous, client-only roadmap progress. Like roadmap.sh, completion is kept
// in the browser (localStorage) — no account, no API, nothing that can error
// server-side.

const key = (slug: string) => `itsh:roadmap:${slug}`;

export function getDone(slug: string): Set<string> {
  if (typeof window === "undefined") return new Set();
  try {
    const raw = window.localStorage.getItem(key(slug));
    return new Set<string>(raw ? JSON.parse(raw) : []);
  } catch {
    return new Set();
  }
}

export function setDone(slug: string, done: Set<string>): void {
  if (typeof window === "undefined") return;
  try {
    window.localStorage.setItem(key(slug), JSON.stringify([...done]));
  } catch {
    /* storage unavailable — progress simply isn't persisted */
  }
}
