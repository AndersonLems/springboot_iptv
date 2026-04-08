import type { AuthSession } from '@/types/media';
import { mockUser } from '@/data/mocks';
// import { apiClient } from './api/client';
// import { API_ENDPOINTS } from './api/endpoints';

const delay = (ms: number) => new Promise(r => setTimeout(r, ms));

export const authService = {
  /**
   * Realiza login com credenciais.
   * Implementação atual: mock local.
   */
  async login(username: string, password: string): Promise<AuthSession> {
    // Implementação atual com mock
    await delay(500);
    if (username === 'admin' && password === 'admin') {
      return { user: mockUser, token: 'mock-token-xyz-123' };
    }
    throw new Error('Credenciais inválidas');

    // Futuramente:
    // const response = await apiClient.post<AuthSession>(API_ENDPOINTS.login, { username, password });
    // return response;
  },

  /**
   * Realiza logout do usuário.
   * Implementação atual: mock local.
   */
  async logout(): Promise<void> {
    // Implementação atual com mock
    await delay(200);

    // Futuramente:
    // await apiClient.post(API_ENDPOINTS.logout);
  },
};
