import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { PageHeader, ErrorBanner, EmptyState, Field } from "../components/ui";

describe("console UI primitives", () => {
  it("PageHeader shows title, description and action", () => {
    render(
      <PageHeader title="Accounts" description="Manage accounts" action={<button>New</button>} />,
    );
    expect(screen.getByRole("heading", { name: "Accounts" })).toBeInTheDocument();
    expect(screen.getByText("Manage accounts")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "New" })).toBeInTheDocument();
  });

  it("ErrorBanner renders an alert", () => {
    render(<ErrorBanner message="Something broke" />);
    expect(screen.getByRole("alert")).toHaveTextContent("Something broke");
  });

  it("EmptyState renders children", () => {
    render(<EmptyState>Nothing here</EmptyState>);
    expect(screen.getByText("Nothing here")).toBeInTheDocument();
  });

  it("Field associates a label and hint", () => {
    render(
      <Field label="Title" hint="be concise">
        <input aria-label="title-input" />
      </Field>,
    );
    expect(screen.getByText("Title")).toBeInTheDocument();
    expect(screen.getByText("be concise")).toBeInTheDocument();
  });
});
