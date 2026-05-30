import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api, ApiError } from "@itsh/api-client";
import { getAccount, getAccessToken } from "@itsh/auth";

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
  const fn = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    calls.push({ url: String(input), init: init ?? {} });
    const res = responses[Math.min(i, responses.length - 1)];
    i += 1;
    return res;
  });
  vi.stubGlobal("fetch", fn);
  return fn;
}

beforeEach(() => {
  calls = [];
  localStorage.clear();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("auth", () => {
  it("login posts credentials and persists the session", async () => {
    const tokens = {
      accessToken: "acc",
      refreshToken: "ref",
      tokenType: "Bearer",
      expiresIn: 900,
      account: {
        id: "a1",
        email: "u@example.com",
        username: "u",
        status: "ACTIVE",
        emailVerified: true,
        provider: null,
        roles: ["ROLE_STUDENT"],
      },
    };
    mockFetch(json(envelope(tokens)));

    await api.login({ identifier: "u", password: "Password1" });

    expect(calls[0].url).toBe("/api/v1/auth/login");
    expect(calls[0].init.method).toBe("POST");
    expect(getAccessToken()).toBe("acc");
    expect(getAccount()?.username).toBe("u");
  });

  it("surfaces backend errors as ApiError", async () => {
    mockFetch(
      json({ status: 409, code: "CONFLICT", message: "Email already registered" }, 409),
    );
    await expect(
      api.register({ email: "x@y.z", username: "x", password: "Password1" }),
    ).rejects.toMatchObject({ status: 409, code: "CONFLICT" });
  });
});

describe("public reads", () => {
  it("unwraps the envelope for a public portfolio", async () => {
    const portfolio = {
      accountId: "a1",
      handle: "aziz",
      visibility: "public",
      publishedAt: "2026-05-01T00:00:00Z",
      certificates: [],
      education: [],
      items: [],
    };
    mockFetch(json(envelope(portfolio)));

    const result = await api.getPublicPortfolio("aziz");

    expect(calls[0].url).toBe("/api/v1/portfolio/public/aziz");
    // public reads carry no Authorization header
    expect((calls[0].init.headers as Record<string, string>)?.Authorization).toBeUndefined();
    expect(result.handle).toBe("aziz");
  });

  it("builds the leaderboard query string", async () => {
    mockFetch(
      json(
        envelope({
          domain: "learning",
          period: "weekly",
          generatedAt: "2026-01-01T00:00:00Z",
          entries: [],
          meta: null,
        }),
      ),
    );

    await api.getLeaderboard({ domain: "learning", period: "weekly", size: 10 });

    expect(calls[0].url).toContain("/api/v1/analytics/rankings?");
    expect(calls[0].url).toContain("domain=learning");
    expect(calls[0].url).toContain("period=weekly");
    expect(calls[0].url).toContain("size=10");
  });

  it("throws ApiError instances with status + code", async () => {
    mockFetch(json({ status: 404, code: "NOT_FOUND", message: "nope" }, 404));
    const err = await api.getPublicPortfolio("ghost").catch((e) => e);
    expect(err).toBeInstanceOf(ApiError);
    expect(err.status).toBe(404);
  });
});
