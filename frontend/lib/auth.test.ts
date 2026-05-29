import { beforeEach, describe, expect, it } from "vitest";
import {
  Account,
  canAuthor,
  clearSession,
  getAccount,
  hasRole,
  isAdmin,
  isAuthenticated,
  saveSession,
} from "./auth";

const account: Account = {
  id: "a1",
  email: "u@example.com",
  username: "u",
  status: "ACTIVE",
  emailVerified: true,
  provider: null,
  roles: ["ROLE_STUDENT"],
};

beforeEach(() => {
  localStorage.clear();
});

describe("session storage", () => {
  it("saves and reads back the session", () => {
    saveSession("access", "refresh", account);
    expect(isAuthenticated()).toBe(true);
    expect(getAccount()?.username).toBe("u");
  });

  it("clears the session", () => {
    saveSession("access", "refresh", account);
    clearSession();
    expect(isAuthenticated()).toBe(false);
    expect(getAccount()).toBeNull();
  });
});

describe("role helpers", () => {
  it("students cannot author and are not admins", () => {
    saveSession("a", "r", account);
    expect(hasRole("ROLE_STUDENT")).toBe(true);
    expect(canAuthor()).toBe(false);
    expect(isAdmin()).toBe(false);
  });

  it("teachers can author but are not admins", () => {
    saveSession("a", "r", { ...account, roles: ["ROLE_TEACHER"] });
    expect(canAuthor()).toBe(true);
    expect(isAdmin()).toBe(false);
  });

  it("admins can author and are admins", () => {
    saveSession("a", "r", { ...account, roles: ["ROLE_ADMIN"] });
    expect(canAuthor()).toBe(true);
    expect(isAdmin()).toBe(true);
  });

  it("treats a missing session as no roles", () => {
    expect(hasRole("ROLE_ADMIN")).toBe(false);
    expect(canAuthor()).toBe(false);
  });
});
