"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";

// A self-contained, MonkeyType-style typing test. Everything runs in the
// browser — no account, no API calls, no persistence. Pick a duration, start
// typing, and watch live WPM/accuracy.

const WORD_BANK =
  ("the be to of and a in that have it for not on with he as you do at this but his by from they we say her she or an will my one all would there their what so up out if about who get which go me when make can like time no just him know take people into year your good some could them see other than then now look only come its over think also back after use two how our work first well way even new want because any these give day most us is are was were code data type test build run loop array string value key index logic syntax method class object module import export return function const let var print input output file system network server client query table column row field model view state props event render style theme color speed focus learn skill level track lesson practice typing accuracy error fix clean simple fast smart goal start finish").split(
    /\s+/,
  );

const DURATIONS = [15, 30, 60] as const;
type Duration = (typeof DURATIONS)[number];

function makeWords(count: number): string[] {
  const out: string[] = [];
  for (let i = 0; i < count; i++) {
    out.push(WORD_BANK[Math.floor(Math.random() * WORD_BANK.length)]);
  }
  return out;
}

export function TypingArena() {
  const [duration, setDuration] = useState<Duration>(30);
  const [words, setWords] = useState<string[]>(() => makeWords(80));
  const [typed, setTyped] = useState<string[]>([""]); // typed text per word
  const [wordIndex, setWordIndex] = useState(0);
  const [startedAt, setStartedAt] = useState<number | null>(null);
  const [remaining, setRemaining] = useState<number>(30);
  const [done, setDone] = useState(false);
  const [focused, setFocused] = useState(false);

  const containerRef = useRef<HTMLDivElement>(null);
  const activeWordRef = useRef<HTMLSpanElement>(null);

  const reset = useCallback(
    (d: Duration = duration) => {
      setWords(makeWords(Math.max(80, d * 4)));
      setTyped([""]);
      setWordIndex(0);
      setStartedAt(null);
      setRemaining(d);
      setDone(false);
      containerRef.current?.focus();
    },
    [duration],
  );

  // Countdown — drives the time-based finish.
  useEffect(() => {
    if (startedAt === null || done) return;
    const id = setInterval(() => {
      const elapsed = (Date.now() - startedAt) / 1000;
      const left = Math.max(0, duration - elapsed);
      setRemaining(left);
      if (left <= 0) {
        setDone(true);
        clearInterval(id);
      }
    }, 100);
    return () => clearInterval(id);
  }, [startedAt, duration, done]);

  // Keep the active word scrolled into view.
  useEffect(() => {
    activeWordRef.current?.scrollIntoView({ block: "center", behavior: "smooth" });
  }, [wordIndex]);

  const onKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "Tab") {
        e.preventDefault();
        reset();
        return;
      }
      if (done) return;

      // Start the clock on the first real keystroke.
      if (startedAt === null && e.key.length === 1) setStartedAt(Date.now());

      if (e.key === "Backspace") {
        e.preventDefault();
        setTyped((prev) => {
          const next = [...prev];
          if (next[wordIndex].length > 0) {
            next[wordIndex] = next[wordIndex].slice(0, -1);
          } else if (wordIndex > 0) {
            setWordIndex(wordIndex - 1);
          }
          return next;
        });
        return;
      }

      if (e.key === " ") {
        e.preventDefault();
        if (typed[wordIndex].length === 0) return; // ignore leading spaces
        setWordIndex((i) => i + 1);
        setTyped((prev) => {
          const next = [...prev];
          if (next[wordIndex + 1] === undefined) next[wordIndex + 1] = "";
          return next;
        });
        return;
      }

      if (e.key.length === 1) {
        e.preventDefault();
        setTyped((prev) => {
          const next = [...prev];
          next[wordIndex] = (next[wordIndex] ?? "") + e.key;
          return next;
        });
      }
    },
    [done, startedAt, wordIndex, typed, reset],
  );

  // Live stats.
  const stats = useMemo(() => {
    let correct = 0;
    let typedChars = 0;
    let correctWords = 0;
    for (let w = 0; w <= wordIndex && w < words.length; w++) {
      const target = words[w] ?? "";
      const got = typed[w] ?? "";
      typedChars += got.length;
      let wordOk = got.length === target.length;
      for (let i = 0; i < got.length; i++) {
        if (got[i] === target[i]) correct++;
        else wordOk = false;
      }
      if (w < wordIndex) {
        typedChars += 1; // the space
        if (wordOk) {
          correct += 1;
          correctWords++;
        }
      }
    }
    const elapsedMin =
      startedAt === null ? 0 : Math.max(1 / 60, (Math.min(Date.now(), startedAt + duration * 1000) - startedAt) / 60000);
    const wpm = elapsedMin > 0 ? Math.round(correct / 5 / elapsedMin) : 0;
    const accuracy = typedChars > 0 ? Math.round((correct / typedChars) * 100) : 100;
    return { wpm, accuracy, correctWords };
  }, [typed, wordIndex, words, startedAt, duration]);

  return (
    <div>
      {/* Controls */}
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div className="inline-flex rounded-xl bg-slate-100 p-1">
          {DURATIONS.map((d) => (
            <button
              key={d}
              onClick={() => {
                setDuration(d);
                reset(d);
              }}
              className={`rounded-lg px-4 py-1.5 text-sm font-medium transition-colors ${
                duration === d ? "bg-white text-brand-700 shadow-soft" : "text-slate-500 hover:text-slate-700"
              }`}
            >
              {d}s
            </button>
          ))}
        </div>
        <div className="flex items-center gap-5">
          <Metric label="time" value={done ? "0" : String(Math.ceil(remaining))} />
          <Metric label="wpm" value={String(stats.wpm)} />
          <Metric label="acc" value={`${stats.accuracy}%`} />
        </div>
      </div>

      {/* Typing surface */}
      <div
        ref={containerRef}
        tabIndex={0}
        onKeyDown={onKeyDown}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        className="relative cursor-text rounded-2xl border border-slate-200 bg-white p-6 shadow-card outline-none ring-brand-400/40 focus:ring-2"
      >
        {done ? (
          <Results
            wpm={stats.wpm}
            accuracy={stats.accuracy}
            words={stats.correctWords}
            duration={duration}
            onRestart={() => reset()}
          />
        ) : (
          <>
            {!focused && startedAt === null && (
              <div className="absolute inset-0 z-10 grid place-items-center rounded-2xl bg-white/70 backdrop-blur-sm">
                <p className="text-sm font-medium text-slate-500">Click here, then start typing</p>
              </div>
            )}
            <div className="flex max-h-44 flex-wrap gap-x-2 gap-y-3 overflow-hidden font-mono text-xl leading-relaxed">
              {words.map((word, w) => {
                const got = typed[w] ?? "";
                const isActive = w === wordIndex;
                const extra = got.slice(word.length);
                return (
                  <span key={w} ref={isActive ? activeWordRef : undefined} className="relative">
                    {word.split("").map((ch, i) => {
                      let cls = "text-slate-300";
                      if (i < got.length) cls = got[i] === ch ? "text-slate-900" : "text-red-500";
                      const caret = isActive && i === got.length;
                      return (
                        <span key={i} className={cls}>
                          {caret && <Caret />}
                          {ch}
                        </span>
                      );
                    })}
                    {extra.split("").map((ch, i) => (
                      <span key={`x${i}`} className="text-red-400/70">
                        {ch}
                      </span>
                    ))}
                    {isActive && got.length >= word.length && <Caret />}
                  </span>
                );
              })}
            </div>
          </>
        )}
      </div>

      <p className="mt-4 text-center text-xs text-slate-400">
        Press <kbd className="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-slate-600">Tab</kbd> to restart · no
        account needed
      </p>
    </div>
  );
}

function Caret() {
  return (
    <span className="absolute -ml-0.5 inline-block h-6 w-0.5 -translate-y-0.5 animate-pulse rounded bg-brand-500 align-middle" />
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="text-center">
      <p className="text-2xl font-bold tabular-nums text-brand-600">{value}</p>
      <p className="text-[11px] uppercase tracking-wide text-slate-400">{label}</p>
    </div>
  );
}

function Results({
  wpm,
  accuracy,
  words,
  duration,
  onRestart,
}: {
  wpm: number;
  accuracy: number;
  words: number;
  duration: number;
  onRestart: () => void;
}) {
  return (
    <div className="py-6 text-center">
      <p className="text-sm font-medium uppercase tracking-wide text-slate-400">Result · {duration}s</p>
      <div className="mt-4 flex items-center justify-center gap-10">
        <div>
          <p className="bg-brand-gradient bg-clip-text text-5xl font-bold text-transparent">{wpm}</p>
          <p className="mt-1 text-xs uppercase tracking-wide text-slate-400">words / min</p>
        </div>
        <div>
          <p className="text-5xl font-bold text-slate-900">{accuracy}%</p>
          <p className="mt-1 text-xs uppercase tracking-wide text-slate-400">accuracy</p>
        </div>
        <div>
          <p className="text-5xl font-bold text-slate-900">{words}</p>
          <p className="mt-1 text-xs uppercase tracking-wide text-slate-400">words</p>
        </div>
      </div>
      <button onClick={onRestart} className="btn-gradient btn-lg mt-8">
        Try again
      </button>
    </div>
  );
}
