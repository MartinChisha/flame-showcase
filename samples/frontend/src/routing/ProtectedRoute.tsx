import type { ReactNode } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import type { UserRole } from '../types/domain';

interface ProtectedRouteProps {
  /** Omit to allow any authenticated user. */
  allowedRoles?: readonly UserRole[];
  children?: ReactNode;
}

// UX-level guard only: the API enforces the same rules authoritatively.
export function ProtectedRoute({ allowedRoles, children }: ProtectedRouteProps) {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) return <FullScreenSpinner />;

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Forbidden />;
  }

  return children ? <>{children}</> : <Outlet />;
}

function FullScreenSpinner() {
  return (
    <div role="status" aria-live="polite" className="grid h-screen place-items-center">
      Loading your workspace...
    </div>
  );
}

function Forbidden() {
  return (
    <section className="mx-auto max-w-md p-8 text-center">
      <h1 className="text-xl font-semibold">Access restricted</h1>
      <p className="mt-2 opacity-80">Your role does not include this area. Contact your league administrator if you need access.</p>
    </section>
  );
}
