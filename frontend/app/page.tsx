import Link from "next/link";
import Nav from "@/components/Nav";

const features = [
  { title: "IT Learning", desc: "Structured tracks in programming, web, and data." },
  { title: "IELTS & SAT", desc: "Realistic mock exams with scoring and analytics." },
  { title: "Typing Practice", desc: "Build speed and accuracy with live stats." },
  { title: "Portfolio", desc: "Showcase certificates, education, and projects." },
];

export default function Home() {
  return (
    <>
      <Nav />
      <main className="mx-auto max-w-5xl px-4">
        <section className="py-20 text-center">
          <h1 className="mx-auto max-w-3xl text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
            Free education for everyone — learn, practice, and prove your skills.
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-slate-600">
            IT-Shaharcha brings IT courses, IELTS/SAT prep, exam simulation, and
            portfolio tracking into one open platform.
          </p>
          <div className="mt-8 flex justify-center gap-3">
            <Link href="/register" className="btn-primary px-6 py-3 text-base">
              Create your free account
            </Link>
            <Link href="/login" className="btn-ghost px-6 py-3 text-base">
              I already have one
            </Link>
          </div>
        </section>

        <section className="grid grid-cols-1 gap-4 pb-24 sm:grid-cols-2 lg:grid-cols-4">
          {features.map((f) => (
            <div key={f.title} className="card">
              <h3 className="font-semibold text-slate-900">{f.title}</h3>
              <p className="mt-2 text-sm text-slate-600">{f.desc}</p>
            </div>
          ))}
        </section>
      </main>
    </>
  );
}
