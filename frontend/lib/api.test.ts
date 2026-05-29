import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api, ApiError } from "./api";

type FetchArgs = { url: string; init: RequestInit };

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function envelope<T>(data: T) {
  return { success: true, message: null, data, timestamp: "2026-01-01T00:00:00Z" };
}

let calls: FetchArgs[];

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

describe("public catalog reads", () => {
  it("builds the tracks path with query params and unwraps the envelope", async () => {
    const page = { items: [{ id: "t1", title: "Java", slug: "java", description: null, courseCount: 2 }], meta: {} };
    mockFetch(jsonResponse(envelope(page)));

    const result = await api.listTracks({ q: "ja", page: 0, size: 20 });

    expect(result.items[0].title).toBe("Java");
    expect(calls[0].url).toBe("/api/v1/learning/tracks?q=ja&page=0&size=20");
    expect(calls[0].init.method ?? "GET").toBe("GET");
  });

  it("omits empty/undefined query values", async () => {
    mockFetch(jsonResponse(envelope({ items: [], meta: {} })));
    await api.listCourses({ trackId: "", level: undefined, size: 50 });
    expect(calls[0].url).toBe("/api/v1/learning/courses?size=50");
  });

  it("does not attach an Authorization header for public reads", async () => {
    localStorage.setItem("itsh.accessToken", "tok");
    mockFetch(jsonResponse(envelope({ id: "c1", modules: [] })));
    await api.getCourse("c1");
    const headers = calls[0].init.headers as Record<string, string>;
    expect(headers?.Authorization).toBeUndefined();
    expect(calls[0].url).toBe("/api/v1/learning/courses/c1");
  });
});

describe("authenticated learner actions", () => {
  it("sends a POST with the bearer token for enroll", async () => {
    localStorage.setItem("itsh.accessToken", "tok123");
    mockFetch(jsonResponse(envelope({ id: "e1", courseId: "c1", status: "active", progressPercent: 0 })));

    const e = await api.enroll("c1");

    expect(e.courseId).toBe("c1");
    expect(calls[0].url).toBe("/api/v1/learning/courses/c1:enroll");
    expect(calls[0].init.method).toBe("POST");
    expect((calls[0].init.headers as Record<string, string>).Authorization).toBe("Bearer tok123");
  });

  it("serializes the completion body", async () => {
    localStorage.setItem("itsh.accessToken", "tok");
    mockFetch(jsonResponse(envelope({ lessonId: "l1", courseProgressPercent: 50 })));

    await api.completeLesson("l1", { courseId: "c1", durationSeconds: 120, scorePercent: 90 });

    expect(calls[0].url).toBe("/api/v1/learning/lessons/l1:complete");
    expect(JSON.parse(calls[0].init.body as string)).toEqual({
      courseId: "c1",
      durationSeconds: 120,
      scorePercent: 90,
    });
  });

  it("returns undefined for empty 204 bodies", async () => {
    localStorage.setItem("itsh.accessToken", "tok");
    mockFetch(new Response(null, { status: 204 }));
    const result = await api.startLesson("l1");
    expect(result).toBeUndefined();
  });
});

describe("error handling", () => {
  it("throws ApiError carrying status, code and field errors", async () => {
    localStorage.setItem("itsh.accessToken", "tok");
    mockFetch(
      jsonResponse(
        {
          status: 400,
          code: "VALIDATION_FAILED",
          message: "Invalid",
          errors: [{ field: "title", message: "required" }],
        },
        400,
      ),
    );

    const err = await api.createTrack({ title: "" }).catch((e) => e);
    expect(err).toBeInstanceOf(ApiError);
    expect(err.status).toBe(400);
    expect(err.code).toBe("VALIDATION_FAILED");
    expect(err.fieldErrors?.[0].field).toBe("title");
  });
});

describe("transparent refresh on 401", () => {
  it("refreshes once then retries the original request", async () => {
    localStorage.setItem("itsh.accessToken", "stale");
    localStorage.setItem("itsh.refreshToken", "refresh-1");

    mockFetch(
      new Response(JSON.stringify({ status: 401, code: "UNAUTHORIZED", message: "nope" }), { status: 401 }),
      jsonResponse(envelope({ accessToken: "fresh", refreshToken: "refresh-2" })),
      jsonResponse(envelope([{ id: "e1", courseId: "c1" }])),
    );

    const result = await api.listMyEnrollments();

    expect(result).toHaveLength(1);
    expect(calls[1].url).toBe("/api/v1/auth/refresh");
    expect(localStorage.getItem("itsh.accessToken")).toBe("fresh");
    // retried call uses the new token
    expect((calls[2].init.headers as Record<string, string>).Authorization).toBe("Bearer fresh");
  });

  it("clears the session when refresh fails", async () => {
    localStorage.setItem("itsh.accessToken", "stale");
    localStorage.setItem("itsh.refreshToken", "refresh-1");

    mockFetch(
      new Response(JSON.stringify({ status: 401, code: "UNAUTHORIZED", message: "nope" }), { status: 401 }),
      new Response(null, { status: 401 }),
    );

    await expect(api.listMyEnrollments()).rejects.toBeInstanceOf(ApiError);
    expect(localStorage.getItem("itsh.accessToken")).toBeNull();
  });
});
