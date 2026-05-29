import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "IT-Shaharcha — Free education platform",
  description: "Learn IT, prep for IELTS & SAT, and build your portfolio — for free.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
