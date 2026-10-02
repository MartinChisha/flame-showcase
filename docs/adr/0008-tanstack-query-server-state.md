# ADR-0008: TanStack Query as the server-state layer

- **Status:** Accepted
- **Date:** 2026-06

## Context

Almost all state in Flame is **server state**: seasons, fixtures, rosters, readiness and standings. The interesting problem is keeping derived views consistent. Submitting a lineup must refresh match-week readiness; submitting a match report must refresh standings, statistics, scouting rankings and readiness.

## Decision

- Use **TanStack Query 5** for all server state, through hooks in the shared `shared-hooks` package.
- Use **React Context** only for auth session and theme.
- Keep form state local with React Hook Form.
- Encode **cross-domain invalidation** in the mutation hooks themselves (for example, `useSubmitLineup` invalidates the match-week, fixture-readiness and week-list keys), so every screen on both platforms gets correct refresh behaviour automatically.

## Alternatives considered

| Option | Why not |
|---|---|
| Redux Toolkit + RTK Query | Capable, but more ceremony; global client state is minimal |
| Zustand + manual fetching | Re-implements caching, de-duplication and invalidation by hand |
| SWR | Lighter, but weaker mutation and invalidation ergonomics for this many related keys |

## Consequences

- Readiness dashboards stay correct without manual refresh buttons.
- About 149 hooks give a discoverable, typed catalogue of everything the UI can do.
- The invalidation rules are product knowledge, so they are documented alongside the hooks ([02 — Architecture](../02-architecture.md#cross-domain-cache-invalidation)).
