import { describe, it, expect, vi, beforeEach } from 'vitest';

describe('API Config', () => {
  beforeEach(() => {
    vi.resetModules();
  });

  it('deve exportar API_BASE_URL como string', async () => {
    const { API_BASE_URL } = await import('@/services/api/config');
    expect(typeof API_BASE_URL).toBe('string');
    expect(API_BASE_URL.length).toBeGreaterThan(0);
  });

  it('deve exportar API_TIMEOUT como número', async () => {
    const { API_TIMEOUT } = await import('@/services/api/config');
    expect(typeof API_TIMEOUT).toBe('number');
    expect(API_TIMEOUT).toBeGreaterThan(0);
  });

  it('deve exportar API_DEFAULTS com headers padrão', async () => {
    const { API_DEFAULTS } = await import('@/services/api/config');
    expect(API_DEFAULTS.headers).toBeDefined();
    expect(API_DEFAULTS.headers['Content-Type']).toBe('application/json');
    expect(API_DEFAULTS.headers.Accept).toBe('application/json');
  });

  it('deve ter um fallback seguro para API_BASE_URL', async () => {
    const { API_BASE_URL } = await import('@/services/api/config');
    expect(API_BASE_URL).toMatch(/^https?:\/\//);
  });
});
