import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi } from '../api/competitionApi';
import { restoreToken, setAccessToken } from '../api/createApiClient';
import type { TokenStore } from '../api/tokenStore';
import type { User } from '../types/domain';

// Platform-agnostic: no DOM, router or native imports. Each app calls
// configureAuth() once at bootstrap with its own storage and navigation.

export type AuthDestination = 'Login' | 'Dashboard';

interface AuthConfig {
  tokenStore: TokenStore;
  onNavigate: (destination: AuthDestination) => void;
  restoreTimeoutMs?: number;
}

let config: AuthConfig | null = null;

export function configureAuth(next: AuthConfig): void {
  config = next;
}

function requireConfig(): AuthConfig {
  if (!config) throw new Error('configureAuth() must run before <AuthProvider> renders.');
  return config;
}

interface AuthState {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  /** Exchange an identity-provider ID token for a Flame session. */
  signInWithIdentityToken: (idToken: string) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('session restore timed out')), ms);
    promise.then(
      (value) => {
        clearTimeout(timer);
        resolve(value);
      },
      (error: unknown) => {
        clearTimeout(timer);
        reject(error);
      },
    );
  });
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const { tokenStore, onNavigate, restoreTimeoutMs = 12_000 } = requireConfig();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const token = await restoreToken();
        if (!token) return;
        const me = await withTimeout(authApi.currentUser(), restoreTimeoutMs);
        if (!cancelled) setUser(me);
      } catch {
        // A stale or rejected token must never strand the user on a spinner.
        setAccessToken(null);
        await tokenStore.removeToken();
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [tokenStore, restoreTimeoutMs]);

  const signInWithIdentityToken = useCallback(
    async (idToken: string) => {
      const { accessToken, user: signedIn } = await authApi.exchangeIdentityToken(idToken);
      setAccessToken(accessToken);
      await tokenStore.setToken(accessToken);
      setUser(signedIn);
      onNavigate('Dashboard');
    },
    [tokenStore, onNavigate],
  );

  const signOut = useCallback(async () => {
    setAccessToken(null);
    await tokenStore.removeToken();
    setUser(null);
    onNavigate('Login');
  }, [tokenStore, onNavigate]);

  const value = useMemo<AuthState>(
    () => ({ user, isLoading, isAuthenticated: user !== null, signInWithIdentityToken, signOut }),
    [user, isLoading, signInWithIdentityToken, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth() must be used inside <AuthProvider>.');
  return ctx;
}
