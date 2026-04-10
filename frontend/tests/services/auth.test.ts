import { describe, it, expect } from 'vitest';
import { authService } from '@/services/auth';

describe('Auth Service', () => {
  it('login salva sessão no localStorage em sucesso', async () => {
    localStorage.clear();
    await authService.login('admin', 'admin');
    const stored = localStorage.getItem('stream_auth_session');
    expect(stored).toContain('admin');
  });

  it('login lança erro em credenciais inválidas', async () => {
    await expect(authService.login('bad', 'bad')).rejects.toThrow('Credenciais invalidas');
  });
});
