import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import type { TokenStore } from './tokenStore';

export interface ApiClientConfig {
  baseURL: string;
  tokenStore: TokenStore;
  /** Called once per 401 so the host app can route to its login screen. */
  onUnauthorized?: () => void;
  timeoutMs?: number;
}

// The token is held in memory for synchronous header injection; the injected
// store is only touched on login, logout, restore and 401.
let accessToken: string | null = null;
let client: AxiosInstance | null = null;
let store: TokenStore | null = null;

export function setAccessToken(token: string | null): void {
  accessToken = token;
}

export function createApiClient(config: ApiClientConfig): AxiosInstance {
  store = config.tokenStore;

  const instance = axios.create({
    baseURL: config.baseURL,
    timeout: config.timeoutMs ?? 15_000,
  });

  instance.interceptors.request.use((req: InternalAxiosRequestConfig) => {
    if (accessToken) {
      req.headers.set('Authorization', `Bearer ${accessToken}`);
    }
    return req;
  });

  instance.interceptors.response.use(
    (res) => res,
    async (error: AxiosError) => {
      if (error.response?.status === 401) {
        setAccessToken(null);
        await store?.removeToken();
        config.onUnauthorized?.();
      }
      return Promise.reject(error);
    },
  );

  client = instance;
  return instance;
}

export function getApiClient(): AxiosInstance {
  if (!client) {
    throw new Error('API client not initialised: call createApiClient() during app bootstrap.');
  }
  return client;
}

export async function restoreToken(): Promise<string | null> {
  const token = (await store?.getToken()) ?? null;
  setAccessToken(token);
  return token;
}

/** Maps transport errors to messages that are safe to show to end users. */
export function toUserMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') return 'The server took too long to respond. Please try again.';
    if (!error.response) return 'Cannot reach the server. Check your connection.';
    const body = error.response.data as { message?: unknown } | undefined;
    if (typeof body?.message === 'string') return body.message;
    if (error.response.status === 403) return 'You do not have permission to do that.';
  }
  return 'Something went wrong. Please try again.';
}
