import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createApiClient } from '../api/createApiClient';
import { sessionTokenStore } from '../api/tokenStore';
import { AuthProvider, configureAuth } from './AuthProvider';
import { routes } from '../routing/routes';

// Web wiring: the shared provider receives web-specific storage and navigation.
// The mobile entry point is identical apart from a keychain-backed token store
// and a React Navigation ref.

const router = createBrowserRouter(routes);

createApiClient({
  baseURL: 'https://api.example.com/api',
  tokenStore: sessionTokenStore,
  onUnauthorized: () => void router.navigate('/login'),
});

configureAuth({
  tokenStore: sessionTokenStore,
  onNavigate: (to) => void router.navigate(to === 'Login' ? '/login' : '/app/dashboard'),
});

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 5 * 60_000, retry: 1 } },
});

const container = document.getElementById('root');
if (container) {
  createRoot(container).render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>,
  );
}
