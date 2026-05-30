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

// ---- Assessment (catalog + authoring) ----

export type ExamType = "IELTS" | "SAT" | "MOCK" | "PRACTICE";
export type QuestionKind =
  | "single_choice"
  | "multiple_choice"
  | "true_false"
  | "short_answer"
  | "essay";

export type Choice = { id: string; text: string };

export type Exam = {
  id: string;
  title: string;
  examType: ExamType;
  description: string | null;
  durationMinutes: number | null;
  isRealExam: boolean;
  sectionCount: number;
};

export type Section = { id: string; name: string; order: number; questionCount: number };

export type Question = {
  id: string;
  prompt: string;
  kind: QuestionKind;
  order: number;
  points: number;
  choices: Choice[] | null;
  correctAnswer: string | null;
  explanation: string | null;
};

export type ExamDetail = Exam & { sections: Section[] };

export type ExamInput = {
  title: string;
  examType: ExamType;
  description?: string;
  durationMinutes?: number;
  isRealExam?: boolean;
};
export type SectionInput = { name: string; order?: number };
export type QuestionInput = {
  prompt: string;
  kind: QuestionKind;
  order?: number;
  points?: number;
  choices?: Choice[];
  correctAnswer?: string;
  explanation?: string;
};

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

// ---- Assessment (candidate session flow) ----

export type SessionStatus = "in_progress" | "submitted" | "scored" | "expired";

export type ExamSession = {
  id: string;
  examId: string;
  accountId: string;
  status: SessionStatus;
  startedAt: string;
  expiresAt: string | null;
  submittedAt: string | null;
};

export type SessionQuestion = {
  id: string;
  prompt: string;
  kind: QuestionKind;
  order: number;
  points: number;
  choices: Choice[] | null;
};

export type Answer = { questionId: string; value: string; sectionId?: string | null };

export type SectionScore = { section: string; score: number; max?: number | null };

export type ExamResult = {
  sessionId: string;
  examId: string;
  accountId: string;
  examType: ExamType;
  scaledScore: number;
  maxScore: number | null;
  sectionScores: SectionScore[] | null;
  scoredAt: string;
  feedback: string | null;
};

// ---- Portfolio (self management) ----

export type FileRef = { fileId: string; contentType: string; sizeBytes: number; url: string | null };

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

  listTutorials(query?: { topic?: string }) {
    return request<Tutorial[]>("/learning/tutorials", { query, auth: false });
  },

  listDocs(query?: { topic?: string }) {
    return request<Doc[]>("/learning/docs", { query, auth: false });
  },

  listTypingLessons(query?: { difficulty?: string }) {
    return request<TypingLesson[]>("/learning/typing/lessons", { query, auth: false });
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
  // Assessment — catalog + authoring
  // ============================================================

  listExams(query?: { examType?: ExamType; page?: number; size?: number }) {
    return request<Page<Exam>>("/assessment/exams", { query, auth: false });
  },

  getExam(examId: string) {
    return request<ExamDetail>(`/assessment/exams/${examId}`, { auth: false });
  },

  createExam(data: ExamInput) {
    return request<Exam>("/assessment/admin/exams", { method: "POST", body: data });
  },
  updateExam(examId: string, data: Partial<ExamInput>) {
    return request<Exam>(`/assessment/admin/exams/${examId}`, { method: "PATCH", body: data });
  },
  deleteExam(examId: string) {
    return request<void>(`/assessment/admin/exams/${examId}`, { method: "DELETE" });
  },

  createSection(examId: string, data: SectionInput) {
    return request<Section>(`/assessment/admin/exams/${examId}/sections`, {
      method: "POST",
      body: data,
    });
  },
  deleteSection(sectionId: string) {
    return request<void>(`/assessment/admin/sections/${sectionId}`, { method: "DELETE" });
  },

  createQuestion(sectionId: string, data: QuestionInput) {
    return request<Question>(`/assessment/admin/sections/${sectionId}/questions`, {
      method: "POST",
      body: data,
    });
  },
  deleteQuestion(questionId: string) {
    return request<void>(`/assessment/admin/questions/${questionId}`, { method: "DELETE" });
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

  listMyTypingSessions() {
    return request<TypingSession[]>("/learning/typing/sessions");
  },

  // ============================================================
  // Learner — assessment candidate flow
  // ============================================================

  startExam(examId: string) {
    return request<ExamSession>(`/assessment/exams/${examId}:start`, { method: "POST" });
  },

  getSession(sessionId: string) {
    return request<ExamSession>(`/assessment/sessions/${sessionId}`);
  },

  getSessionQuestions(sessionId: string) {
    return request<SessionQuestion[]>(`/assessment/sessions/${sessionId}/questions`);
  },

  saveAnswers(sessionId: string, answers: Answer[]) {
    return request<void>(`/assessment/sessions/${sessionId}/answers`, {
      method: "PUT",
      body: { answers },
    });
  },

  submitSession(sessionId: string, answers?: Answer[]) {
    return request<ExamResult>(`/assessment/sessions/${sessionId}:submit`, {
      method: "POST",
      body: answers ? { answers } : undefined,
    });
  },

  getResult(sessionId: string) {
    return request<ExamResult>(`/assessment/sessions/${sessionId}/result`);
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
