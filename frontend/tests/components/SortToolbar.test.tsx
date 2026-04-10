import { render, screen, fireEvent } from "@testing-library/react";
import { useState } from "react";
import { describe, it, expect } from "vitest";
import { SortToolbar } from "@/components/ui/SortToolbar";
import type { SortField, SortOrder } from "@/types/media";

function Wrapper() {
  const [sort, setSort] = useState<SortField>("name");
  const [order, setOrder] = useState<SortOrder>("asc");

  return (
    <div>
      <SortToolbar
        sort={sort}
        order={order}
        onChange={(nextSort, nextOrder) => {
          setSort(nextSort);
          setOrder(nextOrder);
        }}
      />
      <div data-testid="sort">{sort}</div>
      <div data-testid="order">{order}</div>
    </div>
  );
}

describe("SortToolbar", () => {
  it("renderiza 4 opções de ordenação", () => {
    render(<Wrapper />);
    expect(screen.getByText("Nome")).toBeTruthy();
    expect(screen.getByText("Ano")).toBeTruthy();
    expect(screen.getByText("Nota")).toBeTruthy();
    expect(screen.getByText("Popularidade")).toBeTruthy();
  });

  it("chama onChange com SortField correto", () => {
    render(<Wrapper />);
    fireEvent.click(screen.getByText("Ano"));
    expect(screen.getByTestId("sort").textContent).toBe("year");
  });

  it("alterna ordem entre asc e desc", () => {
    render(<Wrapper />);
    const orderButton = screen.getByLabelText("Alterar ordem");
    fireEvent.click(orderButton);
    expect(screen.getByTestId("order").textContent).toBe("desc");
    fireEvent.click(orderButton);
    expect(screen.getByTestId("order").textContent).toBe("asc");
  });
});
