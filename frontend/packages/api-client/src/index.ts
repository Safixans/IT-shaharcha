import {
  Account,
  clearSession,
  getAccessToken,
  getRefreshToken,
  saveSession,
  setAccessToken,
  setRefreshToken,
} from "@itsh/auth";

// Browser calls go to /api/* which Next rewrites to the gateway (same-origin).
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

type QueryValue = string | number | boolean | undefined | null | string[];

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
    if (value === undefined || value === null || value === "") continue;
    if (Array.isArray(value)) {
      // Repeatable params (e.g. ?tags=a&tags=b).
      for (const v of value) if (v !== "") params.append(key, String(v));
    } else {
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

// ---- Server-side fetch (SSR): public endpoints only, straight to the gateway ----
// Next rewrites only apply in the browser, so server components hit the gateway
// directly. Used for SEO pages (published portfolios, public leaderboards).

function serverBase(): string {
  return (
    process.env.INTERNAL_GATEWAY_URL ||
    process.env.GATEWAY_URL ||
    "http://localhost:8080"
  );
}

export async function serverGet<T>(
  path: string,
  query?: Record<string, QueryValue>,
): Promise<T> {
  const res = await fetch(`${serverBase()}${BASE}${buildPath(path, query)}`, {
    headers: { Accept: "application/json" },
    // Public data; let Next cache briefly but stay fresh.
    next: { revalidate: 30 },
  } as RequestInit);
  if (!res.ok) throw await parseError(res);
  const text = await res.text();
  if (!text) return undefined as T;
  return (JSON.parse(text) as ApiEnvelope<T>).data;
}

// ---- Types mirroring the identity contract ----

export type TokenPair = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  account: Account;
};

export type { Account };

export type AccountStatus = "PENDING" | "ACTIVE" | "SUSPENDED" | "DEACTIVATED";

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

// ---- Learning catalog (public) ----

export type CourseLevel = "beginner" | "intermediate" | "advanced";

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

// ---- Roadmaps (public graph catalog) ----

export type RoadmapCard = {
  id: string;
  slug: string;
  title: string;
  tagline: string | null;
  icon: string | null;
  kind: string;
  difficulty: string | null;
  nodeCount: number;
};

export type RoadmapNode = {
  nodeKey: string;
  type: string; // "topic" | "milestone"
  title: string;
  summary: string | null;
  detail: string | null;
  optional: boolean;
  orderIndex: number;
  posX: number | null;
  posY: number | null;
  courseId: string | null;
  courseTitle: string | null;
};

export type RoadmapEdge = {
  fromNodeKey: string;
  toNodeKey: string;
  kind: string; // "sequence" | "branch" | "related"
  style: string; // "solid" | "dotted"
};

export type RoadmapDetail = {
  id: string;
  slug: string;
  title: string;
  tagline: string | null;
  description: string | null;
  icon: string | null;
  kind: string;
  difficulty: string | null;
  layoutMode: string;
  nodes: RoadmapNode[];
  edges: RoadmapEdge[];
};

// ---- Portfolio (public published view) ----

export type VerificationStatus = "PENDING" | "VERIFIED" | "REJECTED";
export type ItemKind = "project" | "award" | "publication" | "experience" | "skill" | "link";
export type Visibility = "private" | "unlisted" | "public";

export type Certificate = {
  id: string;
  accountId: string;
  title: string;
  issuer: string | null;
  issuedOn: string | null;
  fileId: string | null;
  credentialUrl: string | null;
  status: VerificationStatus;
  createdAt: string;
  verifiedAt: string | null;
};

export type Education = {
  id: string;
  accountId: string;
  institution: string;
  degree: string | null;
  fieldOfStudy: string | null;
  startDate: string | null;
  endDate: string | null;
  description: string | null;
};

export type PortfolioItem = {
  id: string;
  accountId: string;
  kind: ItemKind;
  title: string;
  description: string | null;
  url: string | null;
  fileId: string | null;
  tags: string[];
  createdAt: string;
};

export type Portfolio = {
  accountId: string;
  handle: string | null;
  visibility: Visibility;
  publishedAt: string | null;
  certificates: Certificate[];
  education: Education[];
  items: PortfolioItem[];
};

// ---- Analytics (public rankings) ----

export type RankEntry = {
  rank: number;
  accountId: string;
  username: string | null;
  displayName: string | null;
  avatarUrl: string | null;
  domain: string | null;
  points: number;
  level: number | null;
  delta: number | null;
};

export type RankingPeriod = "all_time" | "monthly" | "weekly" | "daily";

export type Leaderboard = {
  domain: string | null;
  period: RankingPeriod;
  generatedAt: string;
  entries: RankEntry[];
  meta: PageMeta | null;
};

export type ServiceDomain = "identity" | "learning" | "assessment" | "portfolio" | "analytics";

// ---- Identity (profile + admin) ----

export type ProfileLink = { label: string; url: string };

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

export type ProfileUpdate = Partial<{
  fullName: string;
  bio: string;
  avatarUrl: string;
  locale: string;
  country: string;
  links: ProfileLink[];
}>;

export type Role = { name: string; description: string | null };

// ---- Learning (full catalog + authoring) ----

export type Lesson = {
  id: string;
  title: string;
  order: number;
  kind: string | null;
  estimatedMinutes: number | null;
};

export type LessonDetail = Lesson & { body: string | null };

export type Module = { id: string; title: string; order: number; lessons: Lesson[] };

export type CourseDetail = Course & { modules: Module[] };

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
};
export type DocInput = {
  title: string;
  topic?: string;
  url?: string;
  body?: string;
  estimatedMinutes?: number;
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

// ---- Assessment (modular training: IELTS L/R/W, SAT modules, quizzes) ----

export type AttemptFamily =
  | "IELTS_LISTENING"
  | "IELTS_READING"
  | "IELTS_WRITING"
  | "SAT"
  | "QUIZ";

export type AttemptStatus =
  | "IN_PROGRESS"
  | "COMPLETED"
  | "PENDING_GRADING"
  | "GRADED"
  | "EXPIRED";

export type ProblemType = "INPUT" | "RADIO" | "SELECT" | "MULTI_SELECT";
export type WritingTask = "TASK_1" | "TASK_2";
export type SatSection = "READING_WRITING" | "MATH";

/** A problem as served to a candidate — correctness withheld. */
export type ServedProblem = {
  problemId: string;
  type: ProblemType;
  prompt: string | null;
  options: { id: string; text: string }[] | null;
};

/** Unit summary (browse/list). */
export type UnitMeta = {
  id: string;
  family: AttemptFamily;
  title: string;
  tags: string[];
  active: boolean;
  problemCount: number;
  durationSeconds: number | null;
  satSection?: SatSection | null;
  writingTask?: WritingTask | null;
};

/** Authoring view: unit + its answer-stripped served content. */
export type UnitDetail = UnitMeta & {
  sectionData: string | null;
  /** Authored HTML with answer markers — present only for authors (used to edit/re-parse). */
  originalSectionData?: string | null;
  passage: string | null;
  prompt: string | null;
  audioId: string | null;
  imageId: string | null;
  problems: ServedProblem[];
};

// ---- create / author inputs ----

export type ListeningCreate = {
  title: string;
  tags?: string[];
  questions: string; // authored HTML with answer "blots"
  audioId: string;
  durationSeconds?: number;
};
export type ReadingCreate = {
  title: string;
  tags?: string[];
  passage?: string;
  questions: string; // authored HTML with answer "blots"
  durationSeconds?: number;
};
export type WritingCreate = {
  title: string;
  tags?: string[];
  task: WritingTask;
  prompt: string;
  imageId?: string;
  durationSeconds?: number;
};
export type OptionInput = { text: string; correct: boolean };
export type ObjectiveQuestionInput = {
  type: ProblemType;
  prompt: string;
  explanation?: string;
  options?: OptionInput[]; // for RADIO/SELECT/MULTI_SELECT
  correctAnswers?: string[]; // for INPUT (acceptable answers)
};
export type ObjectiveUnitCreate = {
  title: string;
  tags?: string[];
  satSection?: SatSection; // required for SAT
  durationSeconds?: number;
  questions: ObjectiveQuestionInput[];
};
export type ActivationRequest = { active?: boolean };

// ---- Verification (reviewer) ----

export type VerifyInput = { verified?: boolean; note?: string };

// ---- Learning (learner self-service) ----

export type EnrollmentStatus = "active" | "completed";

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

// ---- Assessment (attempt lifecycle) ----

/**
 * Timezone-proof timing. Count down from `remainingSeconds` with a local monotonic timer;
 * never subtract a server timestamp from the client clock.
 */
export type Timing = {
  startedAt: string;
  endsAt: string;
  serverNow: string;
  remainingSeconds: number;
};

/** A started/resumed attempt: snapshotted content (answer-safe) + timing. */
export type AttemptSession = {
  attemptId: string;
  unitId: string;
  family: AttemptFamily;
  status: AttemptStatus;
  title?: string | null;
  timing: Timing;
  sectionData: string | null;
  passage: string | null;
  prompt: string | null;
  audioId: string | null;
  imageId: string | null;
  problems: ServedProblem[];
};

/** Optimized submit: graded by value, no order index. */
export type AnswerInput = { problemId: string; values: string[] };
export type AttemptSubmit = { answers?: AnswerInput[]; essay?: string };

export type AnswerReport = {
  problemId: string;
  submitted: string[];
  correctOptions: string[];
  correct: boolean;
};

export type WritingCriteria = {
  taskAchievement?: number | null;
  coherenceCohesion?: number | null;
  lexicalResource?: number | null;
  grammaticalRange?: number | null;
};

export type AttemptReport = {
  attemptId: string;
  unitId: string;
  studentId?: string;
  family: AttemptFamily;
  title?: string | null;
  status: AttemptStatus;
  correct: number;
  incorrect: number;
  total: number;
  scorePercent: number | null;
  band: number | null;
  answers: AnswerReport[] | null;
  essay: string | null;
  feedback: string | null;
  criteria: WritingCriteria | null;
  startedAt: string;
  submittedAt: string | null;
  gradedAt: string | null;
};

export type WritingGrade = {
  band: number;
  criteria?: WritingCriteria;
  feedback?: string;
};

// ---- Portfolio (self management) ----

export type FileRef = { fileId: string; contentType: string; sizeBytes: number; url: string | null };

// Attachment service (assessment media: listening audio, writing Task-1 image).
export type AttachmentRef = {
  fileId: string;
  originalName: string;
  contentType: string;
  sizeBytes: number;
};
export type DownloadUrl = {
  url: string;
  expiresInSeconds: number;
  contentType?: string;
  originalName?: string;
};

export type CertificateCreate = {
  title: string;
  issuer?: string;
  issuedOn?: string;
  fileId?: string;
  credentialUrl?: string;
};

export type EducationCreate = {
  institution: string;
  degree?: string;
  fieldOfStudy?: string;
  startDate?: string;
  endDate?: string;
  description?: string;
};

export type PortfolioItemCreate = {
  kind: ItemKind;
  title: string;
  description?: string;
  url?: string;
  fileId?: string;
  tags?: string[];
};

export type PublishInput = { handle?: string; visibility?: "public" | "unlisted" };

// ---- Analytics (cross-domain) ----

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

export type MetricSeries = {
  metric: string;
  unit: string | null;
  aggregation: string | null;
  points: { t: string; v: number }[];
};

export type ProgressOverview = {
  accountId: string;
  generatedAt: string;
  totalPoints: number;
  overallLevel: number | null;
  streakDays: number | null;
  domains: DomainAnalyticsSummary[];
};

export type Milestone = {
  milestone: string;
  points: number;
  domain: string | null;
  accountId: string;
  reachedAt: string;
};

export type Dashboard = {
  accountId: string;
  generatedAt: string;
  summaries: DomainAnalyticsSummary[];
  series: MetricSeries[];
  rank: RankEntry | null;
};

// ---- API ----

export const api = {
  // ---- Auth (public entry) ----

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

  // ---- Public catalog reads (no auth) ----

  listTracks(query?: { q?: string; page?: number; size?: number }) {
    return request<Page<Track>>("/learning/tracks", { query, auth: false });
  },

  listCourses(query?: { trackId?: string; level?: CourseLevel; page?: number; size?: number }) {
    return request<Page<Course>>("/learning/courses", { query, auth: false });
  },

  listRoadmaps(query?: { q?: string; kind?: string; page?: number; size?: number }) {
    return request<Page<RoadmapCard>>("/learning/roadmaps", { query, auth: false });
  },

  getRoadmap(slug: string) {
    return request<RoadmapDetail>(`/learning/roadmaps/${encodeURIComponent(slug)}`, { auth: false });
  },

  // ---- Public portfolio + rankings (no auth) ----

  getPublicPortfolio(handle: string) {
    return request<Portfolio>(`/portfolio/public/${encodeURIComponent(handle)}`, { auth: false });
  },

  getLeaderboard(query?: { domain?: ServiceDomain; period?: RankingPeriod; page?: number; size?: number }) {
    return request<Leaderboard>("/analytics/rankings", { query, auth: false });
  },

  // ---- SSR variants (server components) ----

  serverGetPublicPortfolio(handle: string) {
    return serverGet<Portfolio>(`/portfolio/public/${encodeURIComponent(handle)}`);
  },

  serverGetLeaderboard(query?: { domain?: ServiceDomain; period?: RankingPeriod; page?: number; size?: number }) {
    return serverGet<Leaderboard>("/analytics/rankings", query);
  },

  serverListTracks(query?: { q?: string; page?: number; size?: number }) {
    return serverGet<Page<Track>>("/learning/tracks", query);
  },

  // ============================================================
  // Authenticated — profile
  // ============================================================

  myProfile() {
    return request<Profile>("/identity/me");
  },

  updateProfile(data: ProfileUpdate) {
    return request<Profile>("/identity/me", { method: "PATCH", body: data });
  },

  // ============================================================
  // Admin — accounts (ROLE_ADMIN)
  // ============================================================

  listAccounts(query?: { status?: AccountStatus; q?: string; page?: number; size?: number }) {
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

  // ---- Admin — roles ----

  listRoles() {
    return request<Role[]>("/identity/roles");
  },

  createRole(data: { name: string; description?: string }) {
    return request<Role>("/identity/roles", { method: "POST", body: data });
  },

  deleteRole(roleName: string) {
    return request<void>(`/identity/roles/${roleName}`, { method: "DELETE" });
  },

  // ============================================================
  // Learning — catalog reads (authenticated authoring views)
  // ============================================================

  getCourse(courseId: string) {
    return request<CourseDetail>(`/learning/courses/${courseId}`, { auth: false });
  },

  getLesson(lessonId: string) {
    return request<LessonDetail>(`/learning/lessons/${lessonId}`, { auth: false });
  },

  listTutorials(query?: { topic?: string }) {
    return request<Page<Tutorial>>("/learning/tutorials", { query, auth: false }).then((p) => p.items);
  },

  listDocs(query?: { topic?: string }) {
    return request<Page<Doc>>("/learning/docs", { query, auth: false }).then((p) => p.items);
  },

  listTypingLessons(query?: { difficulty?: string }) {
    return request<Page<TypingLesson>>("/learning/typing/lessons", { query, auth: false }).then(
      (p) => p.items,
    );
  },

  // ---- Learning — authoring (TEACHER writes/edits; ADMIN deletes) ----

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
  deleteModule(moduleId: string) {
    return request<void>(`/learning/admin/modules/${moduleId}`, { method: "DELETE" });
  },

  createLesson(data: LessonInput) {
    return request<Lesson>("/learning/admin/lessons", { method: "POST", body: data });
  },
  deleteLesson(lessonId: string) {
    return request<void>(`/learning/admin/lessons/${lessonId}`, { method: "DELETE" });
  },

  createTutorial(data: TutorialInput) {
    return request<Tutorial>("/learning/admin/tutorials", { method: "POST", body: data });
  },
  deleteTutorial(tutorialId: string) {
    return request<void>(`/learning/admin/tutorials/${tutorialId}`, { method: "DELETE" });
  },

  createDoc(data: DocInput) {
    return request<Doc>("/learning/admin/docs", { method: "POST", body: data });
  },
  deleteDoc(docId: string) {
    return request<void>(`/learning/admin/docs/${docId}`, { method: "DELETE" });
  },

  createTypingLesson(data: TypingLessonInput) {
    return request<TypingLesson>("/learning/admin/typing/lessons", { method: "POST", body: data });
  },
  deleteTypingLesson(lessonId: string) {
    return request<void>(`/learning/admin/typing/lessons/${lessonId}`, { method: "DELETE" });
  },

  listSources(query?: { type?: string }) {
    return request<ContentSource[]>("/learning/admin/sources", { query });
  },
  createSource(data: ContentSourceInput) {
    return request<ContentSource>("/learning/admin/sources", { method: "POST", body: data });
  },
  deleteSource(sourceId: string) {
    return request<void>(`/learning/admin/sources/${sourceId}`, { method: "DELETE" });
  },
  syncSource(sourceId: string) {
    return request<SourceSyncRun>(`/learning/admin/sources/${sourceId}:sync`, { method: "POST" });
  },

  // ============================================================
  // Assessment — units (browse + authoring)
  // ============================================================

  // IELTS Listening
  listListening(query?: { tags?: string[]; page?: number; size?: number }) {
    return request<Page<UnitMeta>>("/assessment/ielts/listening", { query });
  },
  getListening(unitId: string) {
    return request<UnitDetail>(`/assessment/ielts/listening/${unitId}`);
  },
  createListening(data: ListeningCreate) {
    return request<UnitDetail>("/assessment/ielts/listening", { method: "POST", body: data });
  },
  updateListening(unitId: string, data: ListeningCreate) {
    return request<UnitDetail>(`/assessment/ielts/listening/${unitId}`, { method: "PUT", body: data });
  },
  deleteListening(unitId: string) {
    return request<void>(`/assessment/ielts/listening/${unitId}`, { method: "DELETE" });
  },
  activateListening(unitId: string, active = true) {
    return request<UnitDetail>(`/assessment/ielts/listening/${unitId}:activate`, {
      method: "POST",
      body: { active } as ActivationRequest,
    });
  },

  // IELTS Reading
  listReading(query?: { tags?: string[]; page?: number; size?: number }) {
    return request<Page<UnitMeta>>("/assessment/ielts/reading", { query });
  },
  getReading(unitId: string) {
    return request<UnitDetail>(`/assessment/ielts/reading/${unitId}`);
  },
  createReading(data: ReadingCreate) {
    return request<UnitDetail>("/assessment/ielts/reading", { method: "POST", body: data });
  },
  updateReading(unitId: string, data: ReadingCreate) {
    return request<UnitDetail>(`/assessment/ielts/reading/${unitId}`, { method: "PUT", body: data });
  },
  deleteReading(unitId: string) {
    return request<void>(`/assessment/ielts/reading/${unitId}`, { method: "DELETE" });
  },
  activateReading(unitId: string, active = true) {
    return request<UnitDetail>(`/assessment/ielts/reading/${unitId}:activate`, {
      method: "POST",
      body: { active } as ActivationRequest,
    });
  },

  // IELTS Writing (teacher-graded)
  listWriting(query?: { tags?: string[]; page?: number; size?: number }) {
    return request<Page<UnitMeta>>("/assessment/ielts/writing", { query });
  },
  getWriting(unitId: string) {
    return request<UnitDetail>(`/assessment/ielts/writing/${unitId}`);
  },
  createWriting(data: WritingCreate) {
    return request<UnitDetail>("/assessment/ielts/writing", { method: "POST", body: data });
  },
  deleteWriting(unitId: string) {
    return request<void>(`/assessment/ielts/writing/${unitId}`, { method: "DELETE" });
  },
  activateWriting(unitId: string, active = true) {
    return request<UnitDetail>(`/assessment/ielts/writing/${unitId}:activate`, {
      method: "POST",
      body: { active } as ActivationRequest,
    });
  },

  // SAT modules (objective)
  listSatModules(query?: { section?: SatSection; tags?: string[]; page?: number; size?: number }) {
    return request<Page<UnitMeta>>("/assessment/sat", { query });
  },
  getSatModule(unitId: string) {
    return request<UnitDetail>(`/assessment/sat/${unitId}`);
  },
  createSatModule(data: ObjectiveUnitCreate) {
    return request<UnitDetail>("/assessment/sat", { method: "POST", body: data });
  },
  deleteSatModule(unitId: string) {
    return request<void>(`/assessment/sat/${unitId}`, { method: "DELETE" });
  },
  activateSat(unitId: string, active = true) {
    return request<UnitDetail>(`/assessment/sat/${unitId}:activate`, {
      method: "POST",
      body: { active } as ActivationRequest,
    });
  },

  // Quizzes (objective)
  listQuizzes(query?: { tags?: string[]; page?: number; size?: number }) {
    return request<Page<UnitMeta>>("/assessment/quizzes", { query });
  },
  getQuiz(unitId: string) {
    return request<UnitDetail>(`/assessment/quizzes/${unitId}`);
  },
  createQuiz(data: ObjectiveUnitCreate) {
    return request<UnitDetail>("/assessment/quizzes", { method: "POST", body: data });
  },
  deleteQuiz(unitId: string) {
    return request<void>(`/assessment/quizzes/${unitId}`, { method: "DELETE" });
  },
  activateQuiz(unitId: string, active = true) {
    return request<UnitDetail>(`/assessment/quizzes/${unitId}:activate`, {
      method: "POST",
      body: { active } as ActivationRequest,
    });
  },

  // ============================================================
  // Assessment — teacher grading (Writing)
  // ============================================================

  gradingQueue(query?: { page?: number; size?: number }) {
    return request<Page<AttemptReport>>("/assessment/grading/queue", { query });
  },
  gradeWriting(attemptId: string, data: WritingGrade) {
    return request<AttemptReport>(`/assessment/grading/${attemptId}`, { method: "POST", body: data });
  },

  // ============================================================
  // Portfolio — reviewer
  // ============================================================

  verifyCertificate(certificateId: string, data: VerifyInput) {
    return request<Certificate>(`/portfolio/certificates/${certificateId}:verify`, {
      method: "POST",
      body: data,
    });
  },

  // ============================================================
  // Learner — learning self-service
  // ============================================================

  enroll(courseId: string) {
    return request<Enrollment>(`/learning/courses/${courseId}:enroll`, { method: "POST" });
  },

  listMyEnrollments() {
    return request<Page<Enrollment>>("/learning/enrollments").then((p) => p.items);
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

  listMyTypingSessions() {
    return request<TypingSession[]>("/learning/typing/sessions");
  },

  // ============================================================
  // Learner — assessment attempts
  // ============================================================

  startListeningAttempt(unitId: string) {
    return request<AttemptSession>(`/assessment/ielts/listening/${unitId}:start`, { method: "POST" });
  },
  startReadingAttempt(unitId: string) {
    return request<AttemptSession>(`/assessment/ielts/reading/${unitId}:start`, { method: "POST" });
  },
  startWritingAttempt(unitId: string) {
    return request<AttemptSession>(`/assessment/ielts/writing/${unitId}:start`, { method: "POST" });
  },
  startSatAttempt(unitId: string) {
    return request<AttemptSession>(`/assessment/sat/${unitId}:start`, { method: "POST" });
  },
  startQuizAttempt(unitId: string) {
    return request<AttemptSession>(`/assessment/quizzes/${unitId}:start`, { method: "POST" });
  },

  /** Start (or resume) an attempt for any unit, given its family. */
  startAttempt(family: AttemptFamily, unitId: string) {
    switch (family) {
      case "IELTS_LISTENING":
        return this.startListeningAttempt(unitId);
      case "IELTS_READING":
        return this.startReadingAttempt(unitId);
      case "IELTS_WRITING":
        return this.startWritingAttempt(unitId);
      case "SAT":
        return this.startSatAttempt(unitId);
      case "QUIZ":
        return this.startQuizAttempt(unitId);
    }
  },

  listMyAttempts(query?: { family?: AttemptFamily; page?: number; size?: number }) {
    return request<Page<AttemptReport>>("/assessment/attempts", { query });
  },
  getAttempt(attemptId: string) {
    return request<AttemptReport>(`/assessment/attempts/${attemptId}`);
  },
  autosaveAttempt(attemptId: string, data: AttemptSubmit) {
    return request<void>(`/assessment/attempts/${attemptId}:autosave`, { method: "POST", body: data });
  },
  submitAttempt(attemptId: string, data?: AttemptSubmit) {
    return request<AttemptReport>(`/assessment/attempts/${attemptId}:submit`, {
      method: "POST",
      body: data ?? {},
    });
  },

  // ============================================================
  // Learner — portfolio self-management
  // ============================================================

  async uploadFile(file: File): Promise<FileRef> {
    const form = new FormData();
    form.append("file", file);
    const token = getAccessToken();
    const headers: Record<string, string> = {};
    if (token) headers["Authorization"] = `Bearer ${token}`;
    const res = await fetch(`${BASE}/portfolio/files`, { method: "POST", headers, body: form });
    if (!res.ok) throw await parseError(res);
    const env = (await res.json()) as ApiEnvelope<FileRef>;
    return env.data;
  },

  // Attachment service (assessment audio/image). These live at /api/* (not /api/v1).
  async uploadAttachment(file: File): Promise<AttachmentRef> {
    const form = new FormData();
    form.append("file", file);
    const token = getAccessToken();
    const headers: Record<string, string> = {};
    if (token) headers["Authorization"] = `Bearer ${token}`;
    const res = await fetch(`/api/upload`, { method: "POST", headers, body: form });
    if (!res.ok) throw await parseError(res);
    const env = (await res.json()) as ApiEnvelope<AttachmentRef>;
    return env.data;
  },

  /** Resolve a presigned, time-limited URL for an attachment fileId (audio/image). */
  async attachmentUrl(fileId: string): Promise<string> {
    const token = getAccessToken();
    const headers: Record<string, string> = { Accept: "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;
    const res = await fetch(`/api/download/${fileId}`, { headers });
    if (!res.ok) throw await parseError(res);
    const env = (await res.json()) as ApiEnvelope<DownloadUrl>;
    return env.data.url;
  },

  listCertificates(query?: { page?: number; size?: number }) {
    return request<Page<Certificate>>("/portfolio/certificates", { query });
  },

  createCertificate(data: CertificateCreate) {
    return request<Certificate>("/portfolio/certificates", { method: "POST", body: data });
  },

  deleteCertificate(certificateId: string) {
    return request<void>(`/portfolio/certificates/${certificateId}`, { method: "DELETE" });
  },

  listEducation() {
    return request<Education[]>("/portfolio/education");
  },

  addEducation(data: EducationCreate) {
    return request<Education>("/portfolio/education", { method: "POST", body: data });
  },

  deleteEducation(educationId: string) {
    return request<void>(`/portfolio/education/${educationId}`, { method: "DELETE" });
  },

  listItems(kind?: ItemKind) {
    return request<PortfolioItem[]>("/portfolio/items", { query: { kind } });
  },

  addItem(data: PortfolioItemCreate) {
    return request<PortfolioItem>("/portfolio/items", { method: "POST", body: data });
  },

  deleteItem(itemId: string) {
    return request<void>(`/portfolio/items/${itemId}`, { method: "DELETE" });
  },

  getMyPortfolio() {
    return request<Portfolio>("/portfolio/me");
  },

  publishPortfolio(data: PublishInput) {
    return request<Portfolio>("/portfolio/me:publish", { method: "POST", body: data });
  },

  // ============================================================
  // Learner — analytics (cross-domain)
  // ============================================================

  getProgress(query?: { accountId?: string; from?: string; to?: string }) {
    return request<ProgressOverview>("/analytics/progress", { query });
  },

  listMilestones(query?: { accountId?: string; page?: number; size?: number }) {
    return request<Page<Milestone>>("/analytics/milestones", { query });
  },

  getMyRank(period?: "all_time" | "monthly" | "weekly" | "daily") {
    return request<RankEntry[]>("/analytics/rankings/me", { query: { period } });
  },

  getDashboard(query?: { accountId?: string; granularity?: string; from?: string; to?: string }) {
    return request<Dashboard>("/analytics/dashboard", { query });
  },
};
