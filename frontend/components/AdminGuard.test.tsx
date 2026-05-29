import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import AdminGuard from "./AdminGuard";
import { saveSession } from "@/lib/auth";

const replace = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace, push: vi.fn() }),
  usePathname: () => "/admin/learning",
}));

function account(roles: string[]) {
  return {
    id: "a1",
    email: "u@example.com",
    username: "u",
    status: "ACTIVE",
    emailVerified: true,
    provider: null,
    roles,
  };
}

beforeEach(() => {
  localStorage.clear();
  replace.mockClear();
});

describe("AdminGuard", () => {
  it("redirects unauthenticated users to login", async () => {
    render(
      <AdminGuard>
        <p>secret tools</p>
      </AdminGuard>,
    );
    await waitFor(() => expect(replace).toHaveBeenCalledWith("/login"));
    expect(screen.queryByText("secret tools")).not.toBeInTheDocument();
  });

  it("denies a signed-in student", async () => {
    saveSession("tok", "ref", account(["ROLE_STUDENT"]));
    render(
      <AdminGuard>
        <p>secret tools</p>
      </AdminGuard>,
    );
    await waitFor(() =>
      expect(screen.getByText(/don't have permission/i)).toBeInTheDocument(),
    );
    expect(screen.queryByText("secret tools")).not.toBeInTheDocument();
  });

  it("renders authoring tools for a teacher", async () => {
    saveSession("tok", "ref", account(["ROLE_TEACHER"]));
    render(
      <AdminGuard>
        <p>secret tools</p>
      </AdminGuard>,
    );
    await waitFor(() => expect(screen.getByText("secret tools")).toBeInTheDocument());
  });
});
