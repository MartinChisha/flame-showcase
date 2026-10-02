# ADR-0010: Capability-based sub-roles instead of new role values

- **Status:** Accepted
- **Date:** 2026-08

## Context

Real organisations have overlapping hats. A referee can also administer a branch; a senior official can oversee several branches read-only; any user can own a marketplace store; a league admin may or may not be trusted with finance. Modelling each combination as a new `UserRole` value would multiply role checks across about 191 endpoints and make dual hats impossible.

## Decision

Keep **five platform roles** (`super_admin`, `league_admin`, `team_manager`, `referee`, `user`). Model everything else as **capabilities derived from assignment records**:

| Capability | Requires role | Derived from |
|---|---|---|
| Branch admin | referee | Branch-admin assignment for a specific branch |
| Mother-body admin (read-only) | referee | Mother-body admin assignment |
| Store owner | any | Approved store ownership |
| Finance mutate | league_admin | Finance access level on the league-admin assignment |

Endpoints check the base role with `@PreAuthorize`; services check the capability **for the specific resource** (this branch, this league).

## Consequences

- The role enum and the method-level rules stay simple and auditable.
- Dual hats work naturally, and read-only oversight can never grant write access by accident.
- The frontend asks lightweight "who am I here?" questions (for example, branch-admin info for the current user) and injects the matching navigation from configuration.
- Authorisation logic is split across two layers, so service-level checks are covered by tests and code-review conventions.
