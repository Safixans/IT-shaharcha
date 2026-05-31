import type { Config } from "tailwindcss";

// Shared IT-Shaharcha design tokens. Keep this file identical across the three
// apps (learner / public / console) so the brand stays consistent.
//
// Brand language: a cyan-blue primary (#009bda) flowing through teal (#63c8cf)
// into pink (#e85aa7), with a purple secondary (#8c4b9e) and a near-black navy
// ink (#020617). Inter typography, generous rounding, and a faint diagonal
// hatch texture give the platform a modern, professional ed-tech feel.
export default {
  content: [
    "./app/**/*.{ts,tsx}",
    "./components/**/*.{ts,tsx}",
    "../../packages/**/src/**/*.{ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#ecf8fd",
          100: "#cfeefb",
          200: "#a3e0f7",
          300: "#69ccf0",
          400: "#28b3e6",
          500: "#009bda",
          600: "#0083bd",
          700: "#0077a8",
          800: "#0a5f86",
          900: "#0e4f6e",
          950: "#072f44",
        },
        accent: {
          50: "#f8f1fb",
          100: "#efddf4",
          400: "#b07cc0",
          500: "#8c4b9e",
          600: "#743f84",
          700: "#5f3469",
        },
        teal: {
          400: "#7ed3d9",
          500: "#63c8cf",
          600: "#45a9b0",
        },
        pink: {
          400: "#f07cbb",
          500: "#e85aa7",
          600: "#cf3f8c",
        },
        ink: "#020617",
      },
      fontFamily: {
        sans: [
          "var(--font-inter)",
          "Inter",
          "ui-sans-serif",
          "system-ui",
          "-apple-system",
          "Segoe UI",
          "Roboto",
          "Helvetica Neue",
          "Arial",
          "sans-serif",
        ],
        mono: [
          "ui-monospace",
          "SFMono-Regular",
          "Menlo",
          "Consolas",
          "Liberation Mono",
          "monospace",
        ],
      },
      borderRadius: {
        "4xl": "2rem",
        "5xl": "2.5rem",
      },
      boxShadow: {
        soft: "0 1px 2px 0 rgb(2 6 23 / 0.04), 0 1px 3px 0 rgb(2 6 23 / 0.06)",
        card: "0 1px 3px 0 rgb(2 6 23 / 0.05), 0 12px 30px -16px rgb(2 6 23 / 0.18)",
        lift: "0 20px 45px -18px rgb(0 155 218 / 0.45)",
        glow: "0 0 0 1px rgb(0 155 218 / 0.12), 0 18px 50px -20px rgb(0 155 218 / 0.40)",
      },
      backgroundImage: {
        "brand-gradient":
          "linear-gradient(135deg, #009bda 0%, #63c8cf 48%, #e85aa7 100%)",
        "brand-gradient-soft":
          "linear-gradient(135deg, rgb(0 155 218 / 0.14) 0%, rgb(99 200 207 / 0.12) 50%, rgb(232 90 167 / 0.14) 100%)",
        "ink-gradient":
          "linear-gradient(135deg, #020617 0%, #0e4f6e 55%, #0a5f86 100%)",
        "hero-grid":
          "radial-gradient(60% 55% at 50% 0%, rgb(0 155 218 / 0.14) 0%, rgb(0 155 218 / 0) 70%)",
        hatch:
          "repeating-linear-gradient(135deg, rgb(2 6 23 / 0.04) 0, rgb(2 6 23 / 0.04) 1px, transparent 1px, transparent 16px)",
      },
      keyframes: {
        "fade-up": {
          "0%": { opacity: "0", transform: "translateY(8px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        float: {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-8px)" },
        },
      },
      animation: {
        "fade-up": "fade-up 0.4s ease-out both",
        float: "float 6s ease-in-out infinite",
      },
    },
  },
  plugins: [],
} satisfies Config;
