import { render, screen, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { SearchPage } from "@/routes/_authenticated/search";

describe("SearchPage", () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    localStorage.setItem("stream_auth_session", JSON.stringify({ token: "test" }));
  });

  afterEach(() => {
    queryClient.clear();
  });

  it("não busca com menos de 2 caracteres", async () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch");
    render(
      <QueryClientProvider client={queryClient}>
        <SearchPage />
      </QueryClientProvider>,
    );

    const user = userEvent.setup();
    const input = screen.getByRole("textbox");
    await user.type(input, "a");

    await act(async () => {
      await new Promise((r) => setTimeout(r, 450));
    });

    expect(fetchSpy).not.toHaveBeenCalled();
    fetchSpy.mockRestore();
  });

  it("mostra 3 seções de resultado", async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <SearchPage />
      </QueryClientProvider>,
    );

    const user = userEvent.setup();
    const input = screen.getByRole("textbox");
    await user.type(input, "ca");

    await act(async () => {
      await new Promise((r) => setTimeout(r, 450));
    });

    expect(await screen.findByText("Filmes")).toBeTruthy();
    expect(await screen.findByText("Series")).toBeTruthy();
    expect(await screen.findByText("Canais ao Vivo")).toBeTruthy();
  });

  it("esconde seções vazias", async () => {
    const fetchSpy = vi.spyOn(globalThis, "fetch").mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          query: "av",
          movies: [],
          series: [],
          live: [],
          totalResults: 0,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    );

    render(
      <QueryClientProvider client={queryClient}>
        <SearchPage />
      </QueryClientProvider>,
    );

    const user = userEvent.setup();
    const input = screen.getByRole("textbox");
    await user.type(input, "av");

    await act(async () => {
      await new Promise((r) => setTimeout(r, 450));
    });

    expect(screen.queryByText("Filmes")).toBeNull();
    expect(screen.queryByText("Series")).toBeNull();
    expect(screen.queryByText("Canais ao Vivo")).toBeNull();
    fetchSpy.mockRestore();
  });
});
