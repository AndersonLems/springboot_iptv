import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { AuthSession, User } from '@/types/media';
import { getSession, saveSession, clearSession } from '@/lib/auth';
import { authService } from '@/services/auth';

interface AuthContextType {
  session: AuthSession | null;
  user: User | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => getSession());

  const login = useCallback(async (username: string, password: string) => {
    const result = await authService.login(username, password);
    saveSession(result);
    setSession(result);
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        session,
        user: session?.user ?? null,
        isAuthenticated: !!session,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
