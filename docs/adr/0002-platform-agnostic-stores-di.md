# ADR-0002: Platform-agnostic stores through dependency injection

- **Status:** Accepted
- **Date:** 2026-07

## Context

Authentication state (login, signup, session restore, logout, 401 handling) and theme state are needed on both platforms. The implementations differ only in **where the token lives** (`sessionStorage` vs the OS keychain), **how navigation happens** (React Router vs React Navigation) and **where preferences persist**. Importing platform modules into a shared package would break one of the two bundlers.

## Decision

Shared providers depend on small **interfaces**, not on platform modules:

```ts
interface TokenStore {
  getToken(): Promise<string | null>;
  setToken(token: string): Promise<void>;
  removeToken(): Promise<void>;
}
type Navigate = (destination: 'Login' | 'Dashboard') => void;
```

Each app calls `configureAuth({ tokenStore, onNavigate })` and `configureTheme({ storage })` once at startup. The API client factory takes the same token store and an `onUnauthorized` callback.

## Consequences

- One `AuthProvider` with identical behaviour on React DOM and React Native.
- Tests can inject in-memory stores.
- Security-relevant storage choices (session-scoped on web, keychain on mobile) live in each app and are easy to audit.
- Configuration must run before the first render; the providers throw a clear error if they are not configured.

See the clean-room implementation in [`samples/frontend/src/auth`](../../samples/frontend/src/auth/).
