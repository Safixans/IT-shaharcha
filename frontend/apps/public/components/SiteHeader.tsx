import Link from "next/link";

export function SiteHeader() {
  return (
    <header className="sticky top-0 z-30 border-b border-slate-200/70 bg-white/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link href="/" className="flex items-center gap-2.5 font-bold text-ink">
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-brand-gradient text-sm font-bold text-white shadow-lift">
            IT
          </span>
          IT-Shaharcha
        </Link>
        <nav className="flex items-center gap-1 text-sm sm:gap-2">
          <Link href="/#features" className="hidden px-3 py-1.5 font-medium text-slate-600 hover:text-ink sm:block">
            Features
          </Link>
          <Link href="/#about" className="hidden px-3 py-1.5 font-medium text-slate-600 hover:text-ink sm:block">
            How it works
          </Link>
          <Link href="/typing" className="px-3 py-1.5 font-medium text-slate-600 hover:text-ink">
            Typing test
          </Link>
          <Link href="/rankings" className="hidden px-3 py-1.5 font-medium text-slate-600 hover:text-ink sm:block">
            Rankings
          </Link>
          <Link href="/login" className="btn-ghost">
            Log in
          </Link>
          <Link href="/register" className="btn-gradient">
            Get started
          </Link>
        </nav>
      </div>
    </header>
  );
}
