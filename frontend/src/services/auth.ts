import type { AuthSession } from '@/types/media';
const delay = (ms: number) => new Promise(r => setTimeout(r, ms));

export const authService = {
  /**
   * Realiza login com credenciais.
   * Implementação atual: mock local.
   */
  async login(username: string, password: string): Promise<AuthSession> {
    await delay(200);
    if (username !== 'admin' || password !== 'admin') {
      throw new Error('Credenciais invalidas');
    }

    const session: AuthSession = {
      token: 'mock-token',
      user: {
        id: username,
        username,
        displayName: username,
        avatar: '',
        email: '',
      },
    };

    localStorage.setItem('stream_auth_session', JSON.stringify(session));
    return session;
  },

  /**
   * Realiza logout do usuário.
   * Implementação atual: mock local.
   */
  async logout(): Promise<void> {
    // Implementação atual com mock
    await delay(200);
  },
};
