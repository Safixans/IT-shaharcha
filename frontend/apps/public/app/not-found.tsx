import Link from "next/link";
import { SiteHeader } from "../components/SiteHeader";
import { SiteFooter } from "../components/SiteFooter";

export default function NotFound() {
  return (
    <>
      <SiteHeader />
      <main className="flex flex-1 items-center justify-center px-4 py-20 text-center">
        <div>
          <p className="text-sm font-semibold text-brand-600">404</p>
          <h1 className="mt-2 text-2xl font-bold text-slate-900">Page not found</h1>
          <p className="mt-2 text-slate-600">
            The page or portfolio you&apos;re looking for doesn&apos;t exist.
          </p>
          <Link href="/" className="btn-primary mt-6 inline-flex">
            Back home
          </Link>
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
