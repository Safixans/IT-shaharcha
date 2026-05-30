import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: {
    default: "IT-Shaharcha — Free education platform",
    template: "%s — IT-Shaharcha",
  },
  description:
    "Learn IT, prep for IELTS & SAT, and build a verifiable academic portfolio — for free.",
  openGraph: {
    title: "IT-Shaharcha — Free education platform",
    description:
      "Learn IT, prep for IELTS & SAT, and build a verifiable academic portfolio — for free.",
    type: "website",
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="flex min-h-screen flex-col">{children}</body>
    </html>
  );
}
