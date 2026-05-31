import Link from "next/link";

export function SiteFooter() {
  return (
    <footer className="border-t border-slate-200 bg-white">
      <div className="mx-auto max-w-6xl px-4 py-12">
        <div className="flex flex-col items-start justify-between gap-8 sm:flex-row">
          <div className="max-w-sm">
            <Link href="/" className="flex items-center gap-2.5 font-bold text-ink">
              <span className="grid h-9 w-9 place-items-center rounded-xl bg-brand-gradient text-sm font-bold text-white shadow-lift">
                IT
              </span>
              IT-Shaharcha
            </Link>
            <p className="mt-3 text-sm text-slate-500">
              A free education platform — learn IT, prep for IELTS &amp; SAT, and build a verifiable
              academic portfolio.
            </p>
          </div>
          <nav className="grid grid-cols-2 gap-x-12 gap-y-2 text-sm">
            <Link href="/#features" className="text-slate-600 hover:text-ink">Features</Link>
            <Link href="/typing" className="text-slate-600 hover:text-ink">Typing test</Link>
            <Link href="/#about" className="text-slate-600 hover:text-ink">How it works</Link>
            <Link href="/rankings" className="text-slate-600 hover:text-ink">Rankings</Link>
            <Link href="/login" className="text-slate-600 hover:text-ink">Log in</Link>
            <Link href="/register" className="text-slate-600 hover:text-ink">Get started</Link>
          </nav>
        </div>
        <div className="mt-10 border-t border-slate-100 pt-6 text-sm text-slate-400">
          © {new Date().getFullYear()} IT-Shaharcha — free education for everyone.
        </div>
      </div>
    </footer>
  );
}
