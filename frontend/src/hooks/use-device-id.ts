import { useMemo } from "react";

export function useDeviceId(): string {
  return useMemo(() => {
    if (typeof window === "undefined") return "";
    const key = "stream_device_id";
    try {
      const existing = localStorage.getItem(key);
      if (existing) return existing;
      const generated =
        typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
          ? crypto.randomUUID()
          : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
      localStorage.setItem(key, generated);
      return generated;
    } catch {
      return "";
    }
  }, []);
}
