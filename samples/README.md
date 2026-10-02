# Code samples

These samples are **written from scratch** to illustrate the patterns Flame uses in production. They are not extracted from the production codebase and are not wired to any real system. Each one is short, self-contained, and checked in CI.

| Area | Sample | Pattern | Verified by CI |
|---|---|---|---|
| Frontend | [`api/createApiClient.ts`](frontend/src/api/createApiClient.ts) | Axios factory with injected token store and 401 handling | `tsc --noEmit` (strict) |
| Frontend | [`api/tokenStore.ts`](frontend/src/api/tokenStore.ts) | One interface, with web (session), mobile (keychain) and in-memory implementations | `tsc` |
| Frontend | [`auth/AuthProvider.tsx`](frontend/src/auth/AuthProvider.tsx) | Platform-agnostic auth provider configured by dependency injection | `tsc` |
| Frontend | [`auth/bootstrap.web.tsx`](frontend/src/auth/bootstrap.web.tsx) | Wiring the shared provider into a web app | `tsc` |
| Frontend | [`routing/ProtectedRoute.tsx`](frontend/src/routing/ProtectedRoute.tsx), [`withRoleGuard.tsx`](frontend/src/routing/withRoleGuard.tsx) | Role guards for route trees and screen navigators | `tsc` |
| Frontend | [`hooks/useCompetition.ts`](frontend/src/hooks/useCompetition.ts) | Typed TanStack Query hooks with cross-domain invalidation | `tsc` |
| Frontend | [`forms/seasonSchema.ts`](frontend/src/forms/seasonSchema.ts), [`SeasonForm.tsx`](frontend/src/forms/SeasonForm.tsx) | Zod as the single source of truth for validation and form types | `tsc` |
| Frontend | [`navigation/roleNavigation.ts`](frontend/src/navigation/roleNavigation.ts) | Role and capability navigation as configuration | `tsc` |
| Backend | [`RoundRobinScheduler.java`](backend/src/main/java/showcase/competition/RoundRobinScheduler.java) | Circle-method fixture generation, single or double leg, with byes | 7 tests |
| Backend | [`policy/*`](backend/src/main/java/showcase/policy/) | Hierarchical rule resolution plus pluggable evaluators for readiness | 6 tests |
| Backend | [`LineupLockPolicy.java`](backend/src/main/java/showcase/match/LineupLockPolicy.java) | Pure, clock-injected lock rules with a sealed result type | 5 tests |
| Backend | [`SuspensionCalculator.java`](backend/src/main/java/showcase/finance/SuspensionCalculator.java) | Discipline rules: bans that include the current match, double yellow, accumulation, idempotency keys | 6 tests |
| Backend | [`illustrative/`](backend/illustrative/) | Spring conventions: `@PreAuthorize`, service-level scoping, scheduled jobs | Not compiled (needs Spring) |
| Infra | [`terraform/`](infra/terraform/) | Hibernate toggle, no-NAT endpoints, secrets injection, OIDC deploy role | `terraform validate` |
| Infra | [`maintenance.js`](infra/terraform/maintenance.js) | CloudFront Function maintenance mode | — |
| Infra | [`deploy-api.example.yml`](infra/github-actions/deploy-api.example.yml) | OIDC deploy to ECS with immutable, idempotent images | — |
| Infra | [`Dockerfile.api`](infra/docker/Dockerfile.api) | Multi-stage, non-root JRE image with health check | — |

## Running locally

```bash
# Frontend: strict type-check
cd samples/frontend && npm install && npx tsc --noEmit

# Backend: needs only a JDK 21+
cd samples/backend && ./run-tests.sh        # or .\run-tests.ps1 on Windows

# Terraform: validate only, never apply
cd samples/infra/terraform && terraform init -backend=false && terraform validate
```
