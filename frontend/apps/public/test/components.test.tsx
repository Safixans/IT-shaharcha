import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { SiteHeader } from "../components/SiteHeader";
import { AuthShell } from "../components/AuthShell";

describe("SiteHeader", () => {
  it("renders the brand and primary nav links", () => {
    render(<SiteHeader />);
    expect(screen.getByText("IT-Shaharcha")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /log in/i })).toHaveAttribute("href", "/login");
    expect(screen.getByRole("link", { name: /get started/i })).toHaveAttribute("href", "/register");
    expect(screen.getByRole("link", { name: /rankings/i })).toHaveAttribute("href", "/rankings");
  });
});

describe("AuthShell", () => {
  it("renders title, subtitle, children and footer", () => {
    render(
      <AuthShell title="Welcome back" subtitle="Log in to continue" footer={<span>footer-here</span>}>
        <p>form-body</p>
      </AuthShell>,
    );
    expect(screen.getByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
    expect(screen.getByText("Log in to continue")).toBeInTheDocument();
    expect(screen.getByText("form-body")).toBeInTheDocument();
    expect(screen.getByText("footer-here")).toBeInTheDocument();
  });
});
