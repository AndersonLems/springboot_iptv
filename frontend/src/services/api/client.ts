/**
 * API Client preparado para futura integração com backend real.
 *
 * Atualmente NÃO é utilizado — os services consomem mocks locais.
 * Quando a API estiver pronta, basta importar `apiClient` nos services
 * e substituir os retornos mockados pelas chamadas reais.
 */

import { API_BASE_URL, API_TIMEOUT, API_DEFAULTS } from "./config";

interface RequestOptions extends RequestInit {
  timeout?: number;
}

const deviceId = (() => {
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
})();

class ApiClient {
  private baseURL: string;
  private timeout: number;
  private defaultHeaders: Record<string, string>;

  constructor(
    baseURL: string,
    timeout: number,
    headers: Record<string, string>,
  ) {
    this.baseURL = baseURL;
    this.timeout = timeout;
    this.defaultHeaders = headers;
  }

  private getAuthToken(): string | null {
    if (typeof window === "undefined") return null;
    try {
      const session = localStorage.getItem("stream_auth_session");
      if (session) {
        const parsed = JSON.parse(session);
        return parsed.token ?? null;
      }
    } catch {
      // ignore
    }
    return null;
  }

  private buildHeaders(custom?: HeadersInit): Headers {
    const headers = new Headers(this.defaultHeaders);
    const token = this.getAuthToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    if (deviceId) {
      headers.set("X-Device-Id", deviceId);
    }
    if (custom) {
      const extra = new Headers(custom);
      extra.forEach((value, key) => headers.set(key, value));
    }
    return headers;
  }

  private async request<T>(
    path: string,
    options: RequestOptions = {},
  ): Promise<T> {
    const { timeout = this.timeout, headers: customHeaders, ...rest } = options;
    const url = `${this.baseURL}${path}`;
    const hasTimeout = Number.isFinite(timeout) && timeout > 0;
    const controller = hasTimeout ? new AbortController() : null;
    const timer = hasTimeout
      ? setTimeout(() => controller!.abort(), timeout)
      : null;

    try {
      const response = await fetch(url, {
        ...rest,
        headers: this.buildHeaders(customHeaders),
        signal: controller?.signal ?? rest.signal,
      });

      if (response.status === 401) {
        if (typeof window !== "undefined") {
          localStorage.removeItem("stream_auth_session");
          localStorage.removeItem("stream_device_id");
          window.location.href = "/login";
        }
        throw new Error("Unauthorized");
      }

      if (response.status === 429) {
        const retryAfter = response.headers.get("Retry-After") ?? "60";
        throw new Error(`rate_limited:${retryAfter}`);
      }

      if (!response.ok) {
        const body = await response.text().catch(() => "");
        throw new Error(
          `API Error ${response.status}: ${body || response.statusText}`,
        );
      }

      return (await response.json()) as T;
    } finally {
      if (timer) clearTimeout(timer);
    }
  }

  async get<T>(path: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, { ...options, method: "GET" });
  }

  async post<T>(
    path: string,
    data?: unknown,
    options?: RequestOptions,
  ): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: "POST",
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put<T>(
    path: string,
    data?: unknown,
    options?: RequestOptions,
  ): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: "PUT",
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(path: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, { ...options, method: "DELETE" });
  }
}

export const apiClient = new ApiClient(API_BASE_URL, API_TIMEOUT, {
  ...API_DEFAULTS.headers,
});
