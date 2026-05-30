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
  localStorage.setItem("itsh.accessToken", "tok");
});
afterEach(() => vi.unstubAllGlobals());

describe("course builder — modules & lessons", () => {
  it("creates a module then a lesson", async () => {
    mockFetch(json(envelope({ id: "m1" })), json(envelope({ id: "l1" })));
    await api.createModule({ courseId: "c1", title: "Intro", order: 0 });
    await api.createLesson({ moduleId: "m1", title: "Hello", kind: "reading" });
    expect(calls[0].url).toBe("/api/v1/learning/admin/modules");
    expect(JSON.parse(String(calls[0].init.body))).toMatchObject({ courseId: "c1", title: "Intro" });
    expect(calls[1].url).toBe("/api/v1/learning/admin/lessons");
    expect(JSON.parse(String(calls[1].init.body))).toMatchObject({ moduleId: "m1", kind: "reading" });
  });

  it("deletes a module and a lesson", async () => {
    mockFetch(json(""), json(""));
    await api.deleteModule("m1");
    await api.deleteLesson("l1");
    expect(calls[0].url).toBe("/api/v1/learning/admin/modules/m1");
    expect(calls[0].init.method).toBe("DELETE");
    expect(calls[1].url).toBe("/api/v1/learning/admin/lessons/l1");
  });

  it("loads a course detail without auth header (public read)", async () => {
    mockFetch(json(envelope({ id: "c1", title: "Java", modules: [] })));
    await api.getCourse("c1");
    expect(calls[0].url).toBe("/api/v1/learning/courses/c1");
    expect((calls[0].init.headers as Record<string, string>)?.Authorization).toBeUndefined();
  });
});

describe("content library", () => {
  it("creates tutorials, docs and typing lessons", async () => {
    mockFetch(
      json(envelope({ id: "t1" })),
      json(envelope({ id: "d1" })),
      json(envelope({ id: "y1" })),
    );
    await api.createTutorial({ title: "Intro to Git", videoUrl: "https://v/1" });
    await api.createDoc({ title: "Cheatsheet", url: "https://d/1" });
    await api.createTypingLesson({ title: "Home row", text: "asdf jkl;" });
    expect(calls[0].url).toBe("/api/v1/learning/admin/tutorials");
    expect(calls[1].url).toBe("/api/v1/learning/admin/docs");
    expect(calls[2].url).toBe("/api/v1/learning/admin/typing/lessons");
  });
});

describe("content sources", () => {
  it("creates, syncs and deletes a source", async () => {
    mockFetch(
      json(envelope({ id: "s1" })),
      json(envelope({ sourceId: "s1", runId: "r1", status: "syncing", startedAt: "x", itemsImported: 0 })),
      json(""),
    );
    await api.createSource({ name: "Blog", type: "rss", target: "docs", url: "https://b/feed" });
    const run = await api.syncSource("s1");
    await api.deleteSource("s1");
    expect(calls[0].url).toBe("/api/v1/learning/admin/sources");
    expect(calls[1].url).toBe("/api/v1/learning/admin/sources/s1:sync");
    expect(run.status).toBe("syncing");
    expect(calls[2].init.method).toBe("DELETE");
  });
});

describe("exam settings", () => {
  it("updates an exam via PATCH", async () => {
    mockFetch(json(envelope({ id: "e1", title: "Renamed" })));
    await api.updateExam("e1", { title: "Renamed", examType: "IELTS" });
    expect(calls[0].url).toBe("/api/v1/assessment/admin/exams/e1");
    expect(calls[0].init.method).toBe("PATCH");
  });
});
