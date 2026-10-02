# Frontend samples

In Flame, everything in this folder would live in the shared workspace packages (`shared-types`, `shared-api`, `shared-hooks`, `shared-stores`, `shared-marketing`), and both the React web app and the React Native app would consume it unchanged.

| Folder | Real-world context |
|---|---|
| `types/` | Domain contracts that mirror the API's DTOs. A contract change is one pull request across types, transport, hooks and both UIs. |
| `api/` | The transport tier. The token store is injected because the web app keeps the session in `sessionStorage` and the mobile app keeps it in the OS keychain. |
| `auth/` | The auth provider runs identically on both platforms. Only `bootstrap.web.tsx` knows about the DOM and React Router. |
| `routing/` | `ProtectedRoute` wraps the web route tree; `withRoleGuard` wraps individual screens in React Navigation. Both are usability guards. The API enforces the same rules. |
| `hooks/` | Readiness dashboards depend on lineups, referees and schedules, so mutations invalidate every derived view. That is how both platforms stay consistent without manual refreshes. |
| `forms/` | Zod schemas produce both validation and TypeScript types; Flame has 18 forms built this way. |
| `navigation/` | The web sidebar and mobile tabs render from the same role and capability configuration. |

Run `npm install && npx tsc --noEmit` to type-check under `strict`, `noUncheckedIndexedAccess` and `exactOptionalPropertyTypes`.
