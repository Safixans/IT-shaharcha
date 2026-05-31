import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

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
    <html lang="en" className={inter.variable}>
      <body className="flex min-h-screen flex-col">{children}</body>
    </html>
  );
}
