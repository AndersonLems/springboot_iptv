import { describe, it, expect } from 'vitest';
import { API_ENDPOINTS } from '@/services/api/endpoints';

function assertAllEndpointsStartWithSlash(value: unknown) {
  if (typeof value === 'string') {
    expect(value).toMatch(/^\//);
    return;
  }

  if (typeof value === 'function') {
    // Tenta gerar um endpoint com args dummy (para endpoints parametrizados)
    const fn = value as (...args: any[]) => unknown;
    const arity = fn.length;
    const dummyArgs = Array.from({ length: arity }).map((_, idx) => `x${idx + 1}`);
    assertAllEndpointsStartWithSlash(fn(...dummyArgs));
    return;
  }

  if (value && typeof value === 'object') {
    Object.values(value as Record<string, unknown>).forEach(assertAllEndpointsStartWithSlash);
  }
}

describe('API Endpoints', () => {
  it('deve ter endpoints de autenticação', () => {
    expect(API_ENDPOINTS.login).toBe('/auth/login');
    expect(API_ENDPOINTS.logout).toBe('/auth/logout');
  });

  it('deve ter endpoints de catálogo', () => {
    expect(API_ENDPOINTS.homeFeed).toBeDefined();
    expect(API_ENDPOINTS.movies).toBeDefined();
    expect(API_ENDPOINTS.series).toBeDefined();
    expect(API_ENDPOINTS.channels).toBeDefined();
    expect(API_ENDPOINTS.categories).toBeDefined();
    expect(API_ENDPOINTS.search).toBeDefined();
  });

  it('deve ter endpoints de perfil', () => {
    expect(API_ENDPOINTS.profile).toBeDefined();
    expect(API_ENDPOINTS.myList).toBeDefined();
    expect(API_ENDPOINTS.watchHistory).toBeDefined();
  });

  it('todos os endpoints devem começar com /', () => {
    assertAllEndpointsStartWithSlash(API_ENDPOINTS);
  });
});
