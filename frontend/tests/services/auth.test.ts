import { describe, it, expect, vi } from 'vitest';
import { authService } from '@/services/auth';

describe('Auth Service', () => {
  it('login chama POST /api/auth/login com body correto', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await authService.login('user1', 'pass1');
    const [url, options] = fetchSpy.mock.calls[0];
    expect(new URL(url as string).pathname).toBe('/api/auth/login');
    const body = JSON.parse((options as RequestInit).body as string);
    expect(body).toEqual({ username: 'user1', password: 'pass1' });
    fetchSpy.mockRestore();
  });

  it('login salva sessão no localStorage em sucesso', async () => {
    localStorage.clear();
    await authService.login('user2', 'pass2');
    const stored = localStorage.getItem('stream_auth_session');
    expect(stored).toContain('user2');
  });

  it('login lança erro em 401', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response('Unauthorized', { status: 401 }),
    );
    await expect(authService.login('bad', 'bad')).rejects.toThrow('Credenciais invalidas');
    fetchSpy.mockRestore();
  });
});
