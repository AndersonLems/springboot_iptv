import { describe, it, expect } from 'vitest';
import { API_ENDPOINTS } from '@/services/api/endpoints';

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
    Object.values(API_ENDPOINTS).forEach(endpoint => {
      expect(endpoint).toMatch(/^\//);
    });
  });
});
