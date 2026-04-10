import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, beforeEach, vi } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { HistoryPage } from "@/routes/_authenticated/history";

const now = new Date();

describe("HistoryPage", () => {
  beforeEach(() => {
    localStorage.setItem("stream_auth_session", JSON.stringify({ token: "test" }));
  });

  it("renderiza lista ordenada pelo mais recente", async () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch").mockResolvedValueOnce(
      new Response(
        JSON.stringify([
          {
            channel: {
              id: "old",
              name: "Mais antigo",
              logoUrl: "https://img/old.png",
              groupTitle: "Grupo",
              streamUrl: "https://old",
              duration: 1000,
            },
            watchedAt: new Date(now.getTime() - 1000 * 60 * 60 * 5).toISOString(),
          },
          {
            channel: {
              id: "new",
              name: "Mais recente",
              logoUrl: "https://img/new.png",
              groupTitle: "Grupo",
              streamUrl: "https://new",
              duration: 1000,
            },
            watchedAt: new Date(now.getTime() - 1000 * 60).toISOString(),
          },
        ]),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    );

    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <HistoryPage />
      </QueryClientProvider>,
    );

    const items = await screen.findAllByText(/Mais/);
    expect(items[0]?.textContent).toContain("Mais recente");
    fetchSpy.mockRestore();
  });

  it("botão remover chama DELETE", async () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
      const url =
        typeof input === "string"
          ? input
          : input instanceof URL
            ? input.toString()
            : (input as Request).url;
      const method = (init?.method ?? "GET").toUpperCase();

      if (method === "GET" && url.includes("/api/history")) {
        return new Response(
          JSON.stringify([
            {
              channel: {
                id: "pl-1",
                name: "Canal Ação",
                logoUrl: "https://img/pl1.png",
                groupTitle: "Filmes | Ação",
                streamUrl: "https://pl/1",
                duration: 7200,
              },
              watchedAt: new Date(now.getTime() - 1000 * 60).toISOString(),
            },
          ]),
          { status: 200, headers: { "Content-Type": "application/json" } },
        );
      }

      if (method === "DELETE" && url.includes("/api/history")) {
        return new Response(JSON.stringify(null), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        });
      }

      return new Response(JSON.stringify([]), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    });
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <HistoryPage />
      </QueryClientProvider>,
    );

    const removeButton = await screen.findByLabelText("Remover do histórico");
    await userEvent.click(removeButton);
    const lastCall = fetchSpy.mock.calls.find(
      ([url, options]) => (options as RequestInit)?.method === "DELETE",
    );
    expect(lastCall).toBeTruthy();
    fetchSpy.mockRestore();
  });

  it("limpar tudo mostra confirmação", async () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
      const url =
        typeof input === "string"
          ? input
          : input instanceof URL
            ? input.toString()
            : (input as Request).url;
      const method = (init?.method ?? "GET").toUpperCase();

      if (method === "GET" && url.includes("/api/history")) {
        return new Response(
          JSON.stringify([
            {
              channel: {
                id: "pl-1",
                name: "Canal Ação",
                logoUrl: "https://img/pl1.png",
                groupTitle: "Filmes | Ação",
                streamUrl: "https://pl/1",
                duration: 7200,
              },
              watchedAt: new Date(now.getTime() - 1000 * 60).toISOString(),
            },
          ]),
          { status: 200, headers: { "Content-Type": "application/json" } },
        );
      }

      return new Response(JSON.stringify(null), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    });

    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <HistoryPage />
      </QueryClientProvider>,
    );

    const clearButton = await screen.findByText("Limpar tudo");
    await userEvent.click(clearButton);
    expect(await screen.findByText("Limpar histórico")).toBeTruthy();

    fetchSpy.mockRestore();
  });
});
