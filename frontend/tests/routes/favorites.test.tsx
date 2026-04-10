import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, beforeEach, vi } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { FavoritesPage } from "@/routes/_authenticated/favorites";

vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual<typeof import("@tanstack/react-router")>(
    "@tanstack/react-router",
  );
  return {
    ...actual,
    Link: ({ children, ...props }: any) => <a {...props}>{children}</a>,
  };
});

describe("FavoritesPage", () => {
  beforeEach(() => {
    localStorage.setItem("stream_auth_session", JSON.stringify({ token: "test" }));
  });

  it("renderiza lista de favoritos", async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <FavoritesPage />
      </QueryClientProvider>,
    );

    expect(await screen.findByText("Canal Ação")).toBeTruthy();
  });

  it("remove otimista ao clicar", async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <FavoritesPage />
      </QueryClientProvider>,
    );

    const removeButton = await screen.findByLabelText("Remover favorito");
    await userEvent.click(removeButton);
    expect(screen.queryByText("Canal Ação")).toBeNull();
  });

  it("mostra estado vazio quando lista está vazia", async () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch").mockResolvedValueOnce(
      new Response(JSON.stringify([]), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <FavoritesPage />
      </QueryClientProvider>,
    );

    expect(await screen.findByText("Voce ainda nao tem favoritos")).toBeTruthy();
    fetchSpy.mockRestore();
  });
});
