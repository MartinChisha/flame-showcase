import type { RouteObject } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { SeasonForm } from '../forms/SeasonForm';

const Placeholder = ({ title }: { title: string }) => <h1>{title}</h1>;

export const routes: RouteObject[] = [
  { path: '/', element: <Placeholder title="Landing" /> },
  { path: '/login', element: <Placeholder title="Login" /> },
  {
    path: '/app',
    element: <ProtectedRoute />,
    children: [
      { path: 'dashboard', element: <Placeholder title="Dashboard" /> },
      { path: 'standings', element: <Placeholder title="Standings" /> },
      {
        element: <ProtectedRoute allowedRoles={['super_admin', 'league_admin']} />,
        children: [
          { path: 'divisions/:divisionId/seasons/new', element: <SeasonForm divisionId="from-route" /> },
        ],
      },
      {
        element: <ProtectedRoute allowedRoles={['super_admin']} />,
        children: [{ path: 'admin/users', element: <Placeholder title="User management" /> }],
      },
    ],
  },
];
