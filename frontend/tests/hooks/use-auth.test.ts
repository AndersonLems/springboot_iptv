import { describe, it, expect, beforeEach } from 'vitest';
import { getSession, saveSession, clearSession, isAuthenticated } from '@/lib/auth';
import type { AuthSession } from '@/types/media';

const mockSession: AuthSession = {
  user: {
    id: 'u1',
    username: 'admin',
    displayName: 'Admin',
    avatar: 'https://example.com/avatar.jpg',
    email: 'admin@test.com',
  },
  token: 'test-token-123',
};

describe('Auth utils', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('getSession deve retornar null quando não há sessão', () => {
    expect(getSession()).toBeNull();
  });

  it('saveSession deve salvar e getSession deve recuperar', () => {
    saveSession(mockSession);
    const session = getSession();
    expect(session).toEqual(mockSession);
  });

  it('clearSession deve remover a sessão', () => {
    saveSession(mockSession);
    clearSession();
    expect(getSession()).toBeNull();
  });

  it('isAuthenticated deve retornar false sem sessão', () => {
    expect(isAuthenticated()).toBe(false);
  });

  it('isAuthenticated deve retornar true com sessão', () => {
    saveSession(mockSession);
    expect(isAuthenticated()).toBe(true);
  });
});
