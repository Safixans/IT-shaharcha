import {
  Account,
  clearSession,
  getAccessToken,
  getRefreshToken,
  saveSession,
  setAccessToken,
  setRefreshToken,
} from "./auth";

// All requests go to /api/* which Next rewrites to the gateway (same-origin).
const BASE = "/api/v1";

// Success envelope from the backend: { success, message, data, timestamp }
type ApiEnvelope<T> = {
  success: boolean;
  message: string | null;
  data: T;
  timestamp: string;
};

// Error envelope: { timestamp, status, code, message, path, errors }
type ErrorEnvelope = {
  status: number;
  code: string;
  message: string;
  errors?: { field: string; message: string }[];
};

export class ApiError extends Error {
  status: number;
  code: string;
  fieldErrors?: { field: string; message: string }[];

  constructor(status: number, code: string, message: string, fieldErrors?: { field: string; message: string }[]) {
    super(message);
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

async function parseError(res: Response): Promise<ApiError> {
  try {
    const body = (await res.json()) as ErrorEnvelope;
    return new ApiError(
      body.status ?? res.status,
      body.code ?? "ERROR",
      body.message ?? res.statusText,
      body.errors,
    );
  } catch {
    return new ApiError(res.status, "ERROR", res.statusText || "Request failed");
  }
}

type QueryValue = string | number | boolean | undefined | null;

type RequestOptions = {
  method?: string;
  body?: unknown;
  auth?: boolean;
  retry?: boolean;
  query?: Record<string, QueryValue>;
};

function buildPath(path: string, query?: Record<string, QueryValue>): string {
  if (!query) return path;
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== "") {
      params.append(key, String(value));
    }
  }
  const qs = params.toString();
  return qs ? `${path}?${qs}` : path;
}

async function request<T>(path: string, opts: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, auth = true, retry = true, query } = opts;

  const headers: Record<string, string> = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (auth) {
    const token = getAccessToken();
    if (token) headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE}${buildPath(path, query)}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // Transparent refresh-once on 401 for authenticated calls.
  if (res.status === 401 && auth && retry) {
    const refreshed = await tryRefresh();
    if (refreshed) {
      return request<T>(path, { ...opts, retry: false });
    }
    clearSession();
  }

  if (!res.ok) throw await parseError(res);

  // No-body responses (204 logout, etc.) — guard against empty.
  const text = await res.text();
  if (!text) return undefined as T;
  const envelope = JSON.parse(text) as ApiEnvelope<T>;
  return envelope.data;
}

async function tryRefresh(): Promise<boolean> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;
  try {
    const res = await fetch(`${BASE}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
    if (!res.ok) return false;
    const envelope = (await res.json()) as ApiEnvelope<TokenPair>;
    setAccessToken(envelope.data.accessToken);
    setRefreshToken(envelope.data.refreshToken);
    return true;
  } catch {
    return false;
  }
}

// ---- Types mirroring the identity contract ----

export type TokenPair = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  account: Account;
};

export type AccountStatus = "PENDING" | "ACTIVE" | "SUSPENDED" | "DEACTIVATED";

export type ProfileLink = {
  label: string;
  url: string;
};

export type Profile = {
  accountId: string;
  username: string;
  fullName: string | null;
  bio: string | null;
  avatarUrl: string | null;
  locale: string | null;
  country: string | null;
  links: ProfileLink[];
  updatedAt: string | null;
};

export type ProfileUpdate = {
  fullName?: string;
  bio?: string;
  avatarUrl?: string;
  locale?: string;
  country?: string;
  links?: ProfileLink[];
};

export type Role = {
  name: string;
  description: string | null;
};

export type PageMeta = {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
};

export type Page<T> = {
  items: T[];
  meta: PageMeta;
};

export type DomainAnalyticsSummary = {
  accountId: string;
  domain: string;
  generatedAt: string;
  windowFrom: string | null;
  windowTo: string | null;
  lastActivityAt: string | null;
  points: number;
  level: number | null;
  counters: Record<string, number>;
  streakDays: number | null;
};

export type DomainEvent = {
  eventId: string;
  type: string;
  source: string;
  specVersion: string;
  occurredAt: string;
  recordedAt: string;
  actor: { accountId: string; roles: string[] };
  subject: { type: string; id: string };
  data: Record<string, unknown>;
};

export type MetricSeriesSet = {
  accountId: string;
  domain: string;
  granularity: string;
  series: {
    metric: string;
    unit: string | null;
    aggregation: string | null;
    points: { t: string; v: number }[];
  }[];
};

// ---- Learning domain types ----

export type CourseLevel = "beginner" | "intermediate" | "advanced";
export type EnrollmentStatus = "active" | "completed";

export type Track = {
  id: string;
  title: string;
  slug: string;
  description: string | null;
  courseCount: number;
};

export type Course = {
  id: string;
  trackId: string | null;
  title: string;
  slug: string;
  summary: string | null;
  level: CourseLevel;
  lessonCount: number;
  estimatedMinutes: number | null;
};

export type Lesson = {
  id: string;
  title: string;
  order: number;
  kind: string | null;
  estimatedMinutes: number | null;
};

export type Module = {
  id: string;
  title: string;
  order: number;
  lessons: Lesson[];
};

export type CourseDetail = Course & {
  modules: Module[];
};

export type Enrollment = {
  id: string;
  courseId: string;
  accountId: string;
  status: EnrollmentStatus;
  progressPercent: number;
  enrolledAt: string;
  completedAt: string | null;
};

export type LessonProgress = {
  lessonId: string;
  courseId: string;
  accountId: string;
  completed: boolean;
  scorePercent: number | null;
  courseProgressPercent: number;
  completedAt: string | null;
};

export type Tutorial = {
  id: string;
  title: string;
  topic: string | null;
  videoUrl: string;
  durationSeconds: number | null;
  thumbnailUrl: string | null;
};

export type Doc = {
  id: string;
  title: string;
  topic: string | null;
  url: string;
  estimatedMinutes: number | null;
};

export type TypingLesson = {
  id: string;
  title: string;
  difficulty: string | null;
  text: string;
};

export type TypingSession = {
  id: string;
  accountId: string;
  wpm: number;
  accuracyPercent: number;
  durationSeconds: number;
  keystrokes: number | null;
  lessonId: string | null;
  createdAt: string;
};

export type ContentSource = {
  id: string;
  name: string;
  type: string;
  target: string;
  url: string;
  enabled: boolean;
  schedule: string | null;
  defaultTopic: string | null;
  status: string;
  createdAt: string;
  lastSyncedAt: string | null;
  lastError: string | null;
  itemCount: number;
};

export type SourceSyncRun = {
  sourceId: string;
  runId: string;
  status: string;
  startedAt: string;
  itemsImported: number;
};

// ---- Learning authoring inputs ----

export type TrackInput = { title: string; slug?: string; description?: string };
export type CourseInput = {
  trackId?: string;
  title: string;
  slug?: string;
  summary?: string;
  level: CourseLevel;
  estimatedMinutes?: number;
};
export type ModuleInput = { courseId: string; title: string; order?: number };
export type LessonInput = {
  moduleId: string;
  title: string;
  order?: number;
  kind?: string;
  estimatedMinutes?: number;
  body?: string;
};
export type TutorialInput = {
  title: string;
  topic?: string;
  videoUrl: string;
  durationSeconds?: number;
  thumbnailUrl?: string;
  sourceId?: string;
};
export type DocInput = {
  title: string;
  topic?: string;
  url?: string;
  body?: string;
  estimatedMinutes?: number;
  sourceId?: string;
};
export type TypingLessonInput = { title: string; difficulty?: string; text: string };
export type ContentSourceInput = {
  name: string;
  type: string;
  target: string;
  url: string;
  enabled?: boolean;
  schedule?: string;
  defaultTopic?: string;
};

// ---- Identity API ----

export const api = {
  // ---- Auth ----

  register(data: { email: string; username: string; password: string; fullName?: string }) {
    return request<Account>("/auth/register", { method: "POST", body: data, auth: false });
  },

  async login(data: { identifier: string; password: string }) {
    const tokens = await request<TokenPair>("/auth/login", {
      method: "POST",
      body: data,
      auth: false,
    });
    saveSession(tokens.accessToken, tokens.refreshToken, tokens.account);
    return tokens;
  },

  verify(data: { email: string; code: string }) {
    return request<Account>("/auth/verify", { method: "POST", body: data, auth: false });
  },

  resendOtp(data: { email: string }) {
    return request<void>("/auth/otp:resend", { method: "POST", body: data, auth: false });
  },

  async logout() {
    const refreshToken = getRefreshToken();
    try {
      await request<void>("/auth/logout", {
        method: "POST",
        body: refreshToken ? { refreshToken } : undefined,
      });
    } catch {
      // ignore — clear locally regardless
    }
    clearSession();
  },

  // ---- Profile (self) ----

  myProfile() {
    return request<Profile>("/identity/me");
  },

  updateProfile(data: ProfileUpdate) {
    return request<Profile>("/identity/me", { method: "PATCH", body: data });
  },

  // ---- Accounts (admin) ----

  listAccounts(query?: { status?: AccountStatus; q?: string; page?: number; size?: number; sort?: string }) {
    return request<Page<Account>>("/identity/accounts", { query });
  },

  getAccount(accountId: string) {
    return request<Account>(`/identity/accounts/${accountId}`);
  },

  suspendAccount(accountId: string, reason?: string) {
    return request<Account>(`/identity/accounts/${accountId}:suspend`, {
      method: "POST",
      body: reason ? { reason } : undefined,
    });
  },

  activateAccount(accountId: string) {
    return request<Account>(`/identity/accounts/${accountId}:activate`, { method: "POST" });
  },

  setAccountRoles(accountId: string, roles: string[]) {
    return request<Account>(`/identity/accounts/${accountId}/roles`, {
      method: "PUT",
      body: { roles },
    });
  },

  // ---- Roles (admin) ----

  listRoles() {
    return request<Role[]>("/identity/roles");
  },

  createRole(data: { name: string; description?: string }) {
    return request<Role>("/identity/roles", { method: "POST", body: data });
  },

  deleteRole(roleName: string) {
    return request<void>(`/identity/roles/${roleName}`, { method: "DELETE" });
  },

  // ---- Analytics ----

  analyticsSummary(query?: { accountId?: string; from?: string; to?: string }) {
    return request<DomainAnalyticsSummary>("/identity/analytics/summary", { query });
  },

  analyticsActivity(query?: { accountId?: string; from?: string; to?: string; page?: number; size?: number; sort?: string }) {
    return request<Page<DomainEvent>>("/identity/analytics/activity", { query });
  },

  analyticsMetrics(query?: { accountId?: string; metric?: string; granularity?: string; from?: string; to?: string }) {
    return request<MetricSeriesSet>("/identity/analytics/metrics", { query });
  },

  // ---- Learning: public catalog ----

  listTracks(query?: { q?: string; page?: number; size?: number }) {
    return request<Page<Track>>("/learning/tracks", { query, auth: false });
  },

  listCourses(query?: { trackId?: string; level?: CourseLevel; page?: number; size?: number }) {
    return request<Page<Course>>("/learning/courses", { query, auth: false });
  },

  getCourse(courseId: string) {
    return request<CourseDetail>(`/learning/courses/${courseId}`, { auth: false });
  },

  listTutorials(query?: { topic?: string }) {
    return request<Tutorial[]>("/learning/tutorials", { query, auth: false });
  },

  listDocs(query?: { topic?: string }) {
    return request<Doc[]>("/learning/docs", { query, auth: false });
  },

  listTypingLessons(query?: { difficulty?: string }) {
    return request<TypingLesson[]>("/learning/typing/lessons", { query, auth: false });
  },

  // ---- Learning: learner self-service ----

  enroll(courseId: string) {
    return request<Enrollment>(`/learning/courses/${courseId}:enroll`, { method: "POST" });
  },

  listMyEnrollments() {
    return request<Enrollment[]>("/learning/enrollments");
  },

  startLesson(lessonId: string) {
    return request<void>(`/learning/lessons/${lessonId}:start`, { method: "POST" });
  },

  completeLesson(
    lessonId: string,
    data: { courseId: string; moduleId?: string; durationSeconds: number; scorePercent?: number },
  ) {
    return request<LessonProgress>(`/learning/lessons/${lessonId}:complete`, {
      method: "POST",
      body: data,
    });
  },

  recordWatched(
    tutorialId: string,
    data: { watchedSeconds: number; positionSeconds?: number; completed?: boolean },
  ) {
    return request<void>(`/learning/tutorials/${tutorialId}:watched`, { method: "POST", body: data });
  },

  recordRead(docId: string, data?: { durationSeconds?: number; scrollPercent?: number }) {
    return request<void>(`/learning/docs/${docId}:read`, { method: "POST", body: data });
  },

  submitTypingSession(data: {
    wpm: number;
    accuracyPercent: number;
    durationSeconds: number;
    keystrokes?: number;
    lessonId?: string;
  }) {
    return request<TypingSession>("/learning/typing/sessions", { method: "POST", body: data });
  },

  listMyTypingSessions(query?: { from?: string; to?: string }) {
    return request<TypingSession[]>("/learning/typing/sessions", { query });
  },

  learningAnalyticsSummary(query?: { accountId?: string; from?: string; to?: string }) {
    return request<DomainAnalyticsSummary>("/learning/analytics/summary", { query });
  },

  learningAnalyticsActivity(query?: { accountId?: string; from?: string; to?: string; page?: number; size?: number; sort?: string }) {
    return request<Page<DomainEvent>>("/learning/analytics/activity", { query });
  },

  learningAnalyticsMetrics(query?: { accountId?: string; metric?: string; granularity?: string; from?: string; to?: string }) {
    return request<MetricSeriesSet>("/learning/analytics/metrics", { query });
  },

  // ---- Learning: admin authoring ----

  createTrack(data: TrackInput) {
    return request<Track>("/learning/admin/tracks", { method: "POST", body: data });
  },

  updateTrack(trackId: string, data: Partial<TrackInput>) {
    return request<Track>(`/learning/admin/tracks/${trackId}`, { method: "PATCH", body: data });
  },

  deleteTrack(trackId: string) {
    return request<void>(`/learning/admin/tracks/${trackId}`, { method: "DELETE" });
  },

  createCourse(data: CourseInput) {
    return request<Course>("/learning/admin/courses", { method: "POST", body: data });
  },

  updateCourse(courseId: string, data: Partial<CourseInput>) {
    return request<Course>(`/learning/admin/courses/${courseId}`, { method: "PATCH", body: data });
  },

  deleteCourse(courseId: string) {
    return request<void>(`/learning/admin/courses/${courseId}`, { method: "DELETE" });
  },

  createModule(data: ModuleInput) {
    return request<Module>("/learning/admin/modules", { method: "POST", body: data });
  },

  updateModule(moduleId: string, data: Partial<ModuleInput>) {
    return request<Module>(`/learning/admin/modules/${moduleId}`, { method: "PATCH", body: data });
  },

  deleteModule(moduleId: string) {
    return request<void>(`/learning/admin/modules/${moduleId}`, { method: "DELETE" });
  },

  createLesson(data: LessonInput) {
    return request<Lesson>("/learning/admin/lessons", { method: "POST", body: data });
  },

  updateLesson(lessonId: string, data: Partial<LessonInput>) {
    return request<Lesson>(`/learning/admin/lessons/${lessonId}`, { method: "PATCH", body: data });
  },

  deleteLesson(lessonId: string) {
    return request<void>(`/learning/admin/lessons/${lessonId}`, { method: "DELETE" });
  },

  createTutorial(data: TutorialInput) {
    return request<Tutorial>("/learning/admin/tutorials", { method: "POST", body: data });
  },

  updateTutorial(tutorialId: string, data: Partial<TutorialInput>) {
    return request<Tutorial>(`/learning/admin/tutorials/${tutorialId}`, { method: "PATCH", body: data });
  },

  deleteTutorial(tutorialId: string) {
    return request<void>(`/learning/admin/tutorials/${tutorialId}`, { method: "DELETE" });
  },

  createDoc(data: DocInput) {
    return request<Doc>("/learning/admin/docs", { method: "POST", body: data });
  },

  updateDoc(docId: string, data: Partial<DocInput>) {
    return request<Doc>(`/learning/admin/docs/${docId}`, { method: "PATCH", body: data });
  },

  deleteDoc(docId: string) {
    return request<void>(`/learning/admin/docs/${docId}`, { method: "DELETE" });
  },

  createTypingLesson(data: TypingLessonInput) {
    return request<TypingLesson>("/learning/admin/typing/lessons", { method: "POST", body: data });
  },

  updateTypingLesson(lessonId: string, data: Partial<TypingLessonInput>) {
    return request<TypingLesson>(`/learning/admin/typing/lessons/${lessonId}`, { method: "PATCH", body: data });
  },

  deleteTypingLesson(lessonId: string) {
    return request<void>(`/learning/admin/typing/lessons/${lessonId}`, { method: "DELETE" });
  },

  listSources(query?: { type?: string }) {
    return request<ContentSource[]>("/learning/admin/sources", { query });
  },

  getSource(sourceId: string) {
    return request<ContentSource>(`/learning/admin/sources/${sourceId}`);
  },

  createSource(data: ContentSourceInput) {
    return request<ContentSource>("/learning/admin/sources", { method: "POST", body: data });
  },

  updateSource(sourceId: string, data: Partial<ContentSourceInput>) {
    return request<ContentSource>(`/learning/admin/sources/${sourceId}`, { method: "PATCH", body: data });
  },

  deleteSource(sourceId: string) {
    return request<void>(`/learning/admin/sources/${sourceId}`, { method: "DELETE" });
  },

  syncSource(sourceId: string) {
    return request<SourceSyncRun>(`/learning/admin/sources/${sourceId}:sync`, { method: "POST" });
  },
};
