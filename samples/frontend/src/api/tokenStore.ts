// The only thing that differs between web and mobile token handling is *where*
// the token lives. Shared code depends on this interface; each app injects an
// implementation at startup.

export interface TokenStore {
  getToken(): Promise<string | null>;
  setToken(token: string): Promise<void>;
  removeToken(): Promise<void>;
}

const KEY = 'flame.accessToken';

/** Web: session-scoped, so the token is gone when the tab closes. */
export const sessionTokenStore: TokenStore = {
  async getToken() {
    return sessionStorage.getItem(KEY);
  },
  async setToken(token) {
    sessionStorage.setItem(KEY, token);
  },
  async removeToken() {
    sessionStorage.removeItem(KEY);
  },
};

/** Minimal surface of a native secure-storage module (e.g. the OS keychain). */
export interface SecureStorage {
  getItemAsync(key: string): Promise<string | null>;
  setItemAsync(key: string, value: string): Promise<void>;
  deleteItemAsync(key: string): Promise<void>;
}

/** Mobile: backed by the platform keychain / keystore. */
export function createSecureTokenStore(storage: SecureStorage): TokenStore {
  return {
    getToken: () => storage.getItemAsync(KEY),
    setToken: (token) => storage.setItemAsync(KEY, token),
    removeToken: () => storage.deleteItemAsync(KEY),
  };
}

/** Tests and Storybook. */
export function createMemoryTokenStore(initial: string | null = null): TokenStore {
  let token = initial;
  return {
    getToken: async () => token,
    setToken: async (t) => {
      token = t;
    },
    removeToken: async () => {
      token = null;
    },
  };
}
