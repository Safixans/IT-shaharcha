// Where the authenticated learner app lives. The public app hands off to it
// after login/registration. Configurable per environment.
export const LEARNER_URL =
  process.env.NEXT_PUBLIC_LEARNER_URL || "http://localhost:3000";
