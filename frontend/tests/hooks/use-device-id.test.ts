import { describe, it, expect, beforeEach, vi, afterEach } from "vitest";
import { renderHook } from "@testing-library/react";
import { useDeviceId } from "@/hooks/use-device-id";

describe("useDeviceId", () => {
  const originalCrypto = globalThis.crypto;

  beforeEach(() => {
    localStorage.clear();
    Object.defineProperty(globalThis, "crypto", {
      value: { randomUUID: vi.fn(() => "uuid-123") },
      configurable: true,
    });
  });

  afterEach(() => {
    Object.defineProperty(globalThis, "crypto", {
      value: originalCrypto,
      configurable: true,
    });
  });

  it("gera UUID na primeira chamada", () => {
    const { result } = renderHook(() => useDeviceId());
    expect(result.current).toBe("uuid-123");
  });

  it("persiste no localStorage", () => {
    renderHook(() => useDeviceId());
    expect(localStorage.getItem("stream_device_id")).toBe("uuid-123");
  });

  it("retorna o mesmo ID nas chamadas subsequentes", () => {
    renderHook(() => useDeviceId());
    const { result } = renderHook(() => useDeviceId());
    expect(result.current).toBe("uuid-123");
  });
});
