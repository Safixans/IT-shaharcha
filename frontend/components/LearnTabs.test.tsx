import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import LearnTabs from "./LearnTabs";

const pathname = vi.hoisted(() => ({ value: "/learn" }));
vi.mock("next/navigation", () => ({
  usePathname: () => pathname.value,
}));

function activeClass(label: string): string {
  return screen.getByRole("link", { name: label }).className;
}

describe("LearnTabs", () => {
  it("marks the Courses tab active on /learn", () => {
    pathname.value = "/learn";
    render(<LearnTabs />);
    expect(activeClass("Courses")).toContain("text-brand-600");
    expect(activeClass("Tutorials")).not.toContain("text-brand-600");
  });

  it("keeps Courses active on a course detail route", () => {
    pathname.value = "/learn/courses/abc";
    render(<LearnTabs />);
    expect(activeClass("Courses")).toContain("text-brand-600");
  });

  it("marks Typing active on the typing route", () => {
    pathname.value = "/learn/typing";
    render(<LearnTabs />);
    expect(activeClass("Typing")).toContain("text-brand-600");
    expect(activeClass("Courses")).not.toContain("text-brand-600");
  });
});
