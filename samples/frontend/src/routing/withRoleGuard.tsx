import type { ComponentType, ReactElement } from 'react';
import { useAuth } from '../auth/AuthProvider';
import type { UserRole } from '../types/domain';

interface GuardOptions {
  /** Rendered while the session is restoring. */
  loading: () => ReactElement;
  /** Rendered when the user lacks the role. */
  denied: () => ReactElement;
}

// Higher-order guard for screen-based navigators (React Navigation), where
// there is no route tree to wrap. Renderers are injected so the guard itself
// stays free of platform UI primitives.
export function withRoleGuard<P extends object>(
  Screen: ComponentType<P>,
  allowedRoles: readonly UserRole[],
  { loading: Loading, denied: Denied }: GuardOptions,
): ComponentType<P> {
  function Guarded(props: P) {
    const { user, isLoading } = useAuth();
    if (isLoading) return <Loading />;
    if (!user || !allowedRoles.includes(user.role)) return <Denied />;
    return <Screen {...props} />;
  }
  Guarded.displayName = `withRoleGuard(${Screen.displayName ?? Screen.name ?? 'Screen'})`;
  return Guarded;
}
