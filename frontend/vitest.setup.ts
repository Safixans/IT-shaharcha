import "@testing-library/jest-dom/vitest";
import { afterEach, vi } from "vitest";
import { cleanup } from "@testing-library/react";

// jsdom in this toolchain doesn't expose localStorage; provide a minimal
// in-memory implementation so the token store can be exercised in tests.
if (typeof globalThis.localStorage === "undefined") {
  class MemoryStorage {
    private store = new Map<string, string>();
    get length() {
      return this.store.size;
    }
    getItem(key: string): string | null {
      return this.store.has(key) ? this.store.get(key)! : null;
    }
    setItem(key: string, value: string): void {
      this.store.set(key, String(value));
    }
    removeItem(key: string): void {
      this.store.delete(key);
    }
    clear(): void {
      this.store.clear();
    }
    key(index: number): string | null {
      return Array.from(this.store.keys())[index] ?? null;
    }
  }
  const storage = new MemoryStorage();
  Object.defineProperty(globalThis, "localStorage", { value: storage, configurable: true });
  if (typeof globalThis.window !== "undefined") {
    Object.defineProperty(globalThis.window, "localStorage", { value: storage, configurable: true });
  }
}

afterEach(() => {
  cleanup();
  localStorage.clear();
  vi.restoreAllMocks();
});
