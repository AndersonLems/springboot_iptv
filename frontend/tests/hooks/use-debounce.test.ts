import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useDebounce } from "@/hooks/use-debounce";

describe("useDebounce", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("debounces corretamente após o delay", () => {
    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 300),
      { initialProps: { value: "a" } },
    );

    rerender({ value: "b" });
    expect(result.current).toBe("a");

    act(() => {
      vi.advanceTimersByTime(300);
    });

    expect(result.current).toBe("b");
  });

  it("não dispara antes do delay", () => {
    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 500),
      { initialProps: { value: "a" } },
    );

    rerender({ value: "b" });

    act(() => {
      vi.advanceTimersByTime(200);
    });

    expect(result.current).toBe("a");
  });

  it("dispara imediatamente se delay for 0", () => {
    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 0),
      { initialProps: { value: "a" } },
    );

    rerender({ value: "b" });

    expect(result.current).toBe("b");
  });
});
