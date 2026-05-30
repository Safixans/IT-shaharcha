import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api } from "@itsh/api-client";

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}
function envelope<T>(data: T) {
  return { success: true, message: null, data, timestamp: "2026-01-01T00:00:00Z" };
}

let calls: { url: string; init: RequestInit }[];

function mockFetch(...responses: Response[]) {
  let i = 0;
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      calls.push({ url: String(input), init: init ?? {} });
      const res = responses[Math.min(i, responses.length - 1)];
      i += 1;
      return res;
    }),
  );
}

beforeEach(() => {
  calls = [];
  localStorage.clear();
  localStorage.setItem("itsh.accessToken", "tok"); // authed admin/teacher
});
afterEach(() => vi.unstubAllGlobals());

function authHeader(i = 0) {
  return (calls[i].init.headers as Record<string, string>)?.Authorization;
}

describe("admin — accounts & roles", () => {
  it("lists accounts with filters and bearer auth", async () => {
    mockFetch(json(envelope({ items: [], meta: {} })));
    await api.listAccounts({ status: "ACTIVE", q: "az", size: 50 });
    expect(calls[0].url).toContain("/api/v1/identity/accounts?");
    expect(calls[0].url).toContain("status=ACTIVE");
    expect(calls[0].url).toContain("q=az");
    expect(authHeader()).toBe("Bearer tok");
  });

  it("suspends and activates via colon-verb endpoints", async () => {
    mockFetch(json(envelope({ id: "a1" })), json(envelope({ id: "a1" })));
    await api.suspendAccount("a1", "spam");
    await api.activateAccount("a1");
    expect(calls[0].url).toBe("/api/v1/identity/accounts/a1:suspend");
    expect(calls[0].init.method).toBe("POST");
    expect(calls[1].url).toBe("/api/v1/identity/accounts/a1:activate");
  });

  it("replaces roles with PUT", async () => {
    mockFetch(json(envelope({ id: "a1", roles: ["ROLE_TEACHER"] })));
    await api.setAccountRoles("a1", ["ROLE_TEACHER"]);
    expect(calls[0].url).toBe("/api/v1/identity/accounts/a1/roles");
    expect(calls[0].init.method).toBe("PUT");
    expect(JSON.parse(String(calls[0].init.body))).toEqual({ roles: ["ROLE_TEACHER"] });
  });

  it("creates a role", async () => {
    mockFetch(json(envelope({ name: "ROLE_MOD", description: null })));
    await api.createRole({ name: "ROLE_MOD" });
    expect(calls[0].url).toBe("/api/v1/identity/roles");
    expect(calls[0].init.method).toBe("POST");
  });
});

describe("authoring — learning & assessment", () => {
  it("creates a track", async () => {
    mockFetch(json(envelope({ id: "t1", title: "Java" })));
    await api.createTrack({ title: "Java" });
    expect(calls[0].url).toBe("/api/v1/learning/admin/tracks");
    expect(calls[0].init.method).toBe("POST");
  });

  it("creates an exam then a section then a question", async () => {
    mockFetch(
      json(envelope({ id: "e1" })),
      json(envelope({ id: "s1" })),
      json(envelope({ id: "q1" })),
    );
    await api.createExam({ title: "Mock", examType: "MOCK" });
    await api.createSection("e1", { name: "Reading" });
    await api.createQuestion("s1", { prompt: "2+2?", kind: "single_choice", points: 1 });
    expect(calls[0].url).toBe("/api/v1/assessment/admin/exams");
    expect(calls[1].url).toBe("/api/v1/assessment/admin/exams/e1/sections");
    expect(calls[2].url).toBe("/api/v1/assessment/admin/sections/s1/questions");
  });
});

describe("reviewer — certificate verification", () => {
  it("posts a decision to the verify endpoint", async () => {
    mockFetch(json(envelope({ id: "c1", title: "AWS", status: "VERIFIED" })));
    const cert = await api.verifyCertificate("c1", { verified: true, note: "ok" });
    expect(calls[0].url).toBe("/api/v1/portfolio/certificates/c1:verify");
    expect(calls[0].init.method).toBe("POST");
    expect(cert.status).toBe("VERIFIED");
  });
});
