import { render, screen, fireEvent } from "@testing-library/react";
import { useState } from "react";
import { describe, it, expect } from "vitest";
import { FilterPanel } from "@/components/ui/FilterPanel";
import type { FilterState } from "@/types/media";

const genres = [
  { id: 1, name: "Ação" },
  { id: 2, name: "Drama" },
];

function Wrapper() {
  const [filters, setFilters] = useState<FilterState>({
    yearMin: 1990,
    yearMax: 2024,
    minRating: 5,
    availableOnly: false,
    genres: [],
  });

  return (
    <div>
      <FilterPanel
        filters={filters}
        onChange={setFilters}
        genres={genres}
        open
        onToggle={() => undefined}
      />
      <div data-testid="yearMin">{filters.yearMin}</div>
      <div data-testid="yearMax">{filters.yearMax}</div>
      <div data-testid="minRating">{filters.minRating}</div>
      <div data-testid="availableOnly">{String(filters.availableOnly)}</div>
      <div data-testid="genres">{filters.genres.join(",")}</div>
    </div>
  );
}

describe("FilterPanel", () => {
  it("yearMin/yearMax sliders atualizam FilterState", () => {
    render(<Wrapper />);
    const sliders = screen.getAllByRole("slider");
    fireEvent.change(sliders[0], { target: { value: "2000" } });
    fireEvent.change(sliders[1], { target: { value: "2020" } });
    expect(screen.getByTestId("yearMin").textContent).toBe("2000");
    expect(screen.getByTestId("yearMax").textContent).toBe("2020");
  });

  it("minRating slider atualiza FilterState", () => {
    render(<Wrapper />);
    const sliders = screen.getAllByRole("slider");
    fireEvent.change(sliders[2], { target: { value: "7.5" } });
    expect(screen.getByTestId("minRating").textContent).toBe("7.5");
  });

  it("availableOnly toggle atualiza state", () => {
    render(<Wrapper />);
    const toggle = screen.getByRole("switch");
    fireEvent.click(toggle);
    expect(screen.getByTestId("availableOnly").textContent).toBe("true");
  });

  it("chip de gênero adiciona/remove corretamente", () => {
    render(<Wrapper />);
    const action = screen.getByText("Ação");
    fireEvent.click(action);
    expect(screen.getByTestId("genres").textContent).toBe("1");
    fireEvent.click(action);
    expect(screen.getByTestId("genres").textContent).toBe("");
  });
});
