import Link from "next/link";

export function SiteHeader() {
  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link href="/" className="flex items-center gap-2 font-semibold text-slate-900">
          <span className="grid h-7 w-7 place-items-center rounded-lg bg-brand-500 text-sm font-bold text-white">
            IT
          </span>
          IT-Shaharcha
        </Link>
        <nav className="flex items-center gap-2 text-sm">
          <Link href="/rankings" className="px-3 py-1.5 text-slate-600 hover:text-slate-900">
            Rankings
          </Link>
          <Link href="/login" className="btn-ghost">
            Log in
          </Link>
          <Link href="/register" className="btn-primary">
            Get started
          </Link>
        </nav>
      </div>
    </header>
  );
}
