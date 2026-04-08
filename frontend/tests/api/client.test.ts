import { describe, it, expect } from 'vitest';
import { apiClient } from '@/services/api/client';

describe('API Client', () => {
  it('deve exportar apiClient como objeto', () => {
    expect(apiClient).toBeDefined();
    expect(typeof apiClient).toBe('object');
  });

  it('deve ter métodos HTTP (get, post, put, delete)', () => {
    expect(typeof apiClient.get).toBe('function');
    expect(typeof apiClient.post).toBe('function');
    expect(typeof apiClient.put).toBe('function');
    expect(typeof apiClient.delete).toBe('function');
  });
});
