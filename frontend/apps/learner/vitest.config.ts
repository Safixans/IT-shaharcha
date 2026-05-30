import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import { fileURLToPath } from "node:url";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@itsh/auth": fileURLToPath(new URL("../../packages/auth/src/index.ts", import.meta.url)),
      "@itsh/api-client": fileURLToPath(new URL("../../packages/api-client/src/index.ts", import.meta.url)),
    },
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./vitest.setup.ts"],
    include: ["test/**/*.test.{ts,tsx}"],
  },
});
