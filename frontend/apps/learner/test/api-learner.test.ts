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

describe("learning self-service", () => {
  it("enrolls and completes a lesson", async () => {
    mockFetch(json(envelope({ id: "e1", courseId: "c1", progressPercent: 0 })), json(envelope({ courseProgressPercent: 50 })));
    await api.enroll("c1");
    await api.completeLesson("l1", { courseId: "c1", durationSeconds: 120, scorePercent: 100 });
    expect(calls[0].url).toBe("/api/v1/learning/courses/c1:enroll");
    expect(calls[0].init.method).toBe("POST");
    expect(calls[1].url).toBe("/api/v1/learning/lessons/l1:complete");
    expect(JSON.parse(String(calls[1].init.body))).toMatchObject({ courseId: "c1", durationSeconds: 120 });
  });

  it("submits a typing session", async () => {
    mockFetch(json(envelope({ id: "ts1" })));
    await api.submitTypingSession({ wpm: 60, accuracyPercent: 95, durationSeconds: 30 });
    expect(calls[0].url).toBe("/api/v1/learning/typing/sessions");
  });
});

describe("assessment attempt flow", () => {
  it("starts an attempt, autosaves, and submits by value", async () => {
    mockFetch(
      json(
        envelope({
          attemptId: "at1",
          unitId: "u1",
          family: "QUIZ",
          status: "IN_PROGRESS",
          timing: { startedAt: "t", endsAt: "t", serverNow: "t", remainingSeconds: 600 },
          problems: [{ problemId: "p1", type: "RADIO", prompt: "2+2?", options: [{ id: "1", text: "4" }] }],
        }),
      ),
      json(envelope(null)),
      json(envelope({ attemptId: "at1", status: "COMPLETED", correct: 1, total: 1, scorePercent: 100 })),
    );
    const session = await api.startQuizAttempt("u1");
    await api.autosaveAttempt(session.attemptId, { answers: [{ problemId: "p1", values: ["4"] }] });
    const report = await api.submitAttempt(session.attemptId, {
      answers: [{ problemId: "p1", values: ["4"] }],
    });
    expect(calls[0].url).toBe("/api/v1/assessment/quizzes/u1:start");
    expect(calls[1].url).toBe("/api/v1/assessment/attempts/at1:autosave");
    expect(calls[2].url).toBe("/api/v1/assessment/attempts/at1:submit");
    expect(JSON.parse(String(calls[2].init.body))).toEqual({ answers: [{ problemId: "p1", values: ["4"] }] });
    expect(report.scorePercent).toBe(100);
  });
});

describe("portfolio self-management", () => {
  it("uploads a file as multipart then references it on a certificate", async () => {
    mockFetch(
      json(envelope({ fileId: "f1", contentType: "application/pdf", sizeBytes: 10 })),
      json(envelope({ id: "c1", title: "AWS", status: "PENDING" })),
    );
    const ref = await api.uploadFile(new File(["x"], "cert.pdf", { type: "application/pdf" }));
    await api.createCertificate({ title: "AWS", fileId: ref.fileId });
    expect(calls[0].url).toBe("/api/v1/portfolio/files");
    expect(calls[0].init.body).toBeInstanceOf(FormData);
    // multipart upload must not set a JSON content-type (browser sets the boundary)
    expect((calls[0].init.headers as Record<string, string>)?.["Content-Type"]).toBeUndefined();
    expect(calls[1].url).toBe("/api/v1/portfolio/certificates");
    expect(JSON.parse(String(calls[1].init.body))).toMatchObject({ title: "AWS", fileId: "f1" });
  });

  it("publishes the portfolio", async () => {
    mockFetch(json(envelope({ accountId: "a1", handle: "me", visibility: "public" })));
    await api.publishPortfolio({ handle: "me", visibility: "public" });
    expect(calls[0].url).toBe("/api/v1/portfolio/me:publish");
  });
});

describe("analytics", () => {
  it("reads progress and my rank", async () => {
    mockFetch(json(envelope({ totalPoints: 180, domains: [] })), json(envelope([{ rank: 1, points: 180 }])));
    const p = await api.getProgress();
    const r = await api.getMyRank();
    expect(calls[0].url).toBe("/api/v1/analytics/progress");
    expect(calls[1].url).toContain("/api/v1/analytics/rankings/me");
    expect(p.totalPoints).toBe(180);
    expect(r[0].rank).toBe(1);
  });
});
