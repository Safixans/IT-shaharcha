import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { ProgressBar, PageHeader } from "../components/ui";

describe("learner UI primitives", () => {
  it("PageHeader renders title + description", () => {
    render(<PageHeader title="Learn" description="Browse courses" />);
    expect(screen.getByRole("heading", { name: "Learn" })).toBeInTheDocument();
    expect(screen.getByText("Browse courses")).toBeInTheDocument();
  });

  it("ProgressBar clamps width to 0–100%", () => {
    const { container, rerender } = render(<ProgressBar percent={150} />);
    expect(container.querySelector(".progress-bar")).toHaveStyle({ width: "100%" });
    rerender(<ProgressBar percent={-10} />);
    expect(container.querySelector(".progress-bar")).toHaveStyle({ width: "0%" });
    rerender(<ProgressBar percent={42} />);
    expect(container.querySelector(".progress-bar")).toHaveStyle({ width: "42%" });
  });
});
