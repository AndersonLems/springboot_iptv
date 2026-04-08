import { describe, it, expect } from 'vitest';
import { authService } from '@/services/auth';

describe('Auth Service', () => {
  it('login com admin/admin deve retornar sessão válida', async () => {
    const session = await authService.login('admin', 'admin');
    expect(session).toBeDefined();
    expect(session.user).toBeDefined();
    expect(session.user.username).toBe('admin');
    expect(session.token).toBeDefined();
    expect(typeof session.token).toBe('string');
  });

  it('login com credenciais inválidas deve lançar erro', async () => {
    await expect(authService.login('wrong', 'wrong')).rejects.toThrow('Credenciais inválidas');
  });

  it('login deve retornar user com campos obrigatórios', async () => {
    const session = await authService.login('admin', 'admin');
    expect(session.user.id).toBeDefined();
    expect(session.user.displayName).toBeDefined();
    expect(session.user.email).toBeDefined();
    expect(session.user.avatar).toBeDefined();
  });

  it('logout não deve lançar erro', async () => {
    await expect(authService.logout()).resolves.toBeUndefined();
  });
});
