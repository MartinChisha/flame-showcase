# 03 — Tech Stack

Every layer, the version in production, and why it was chosen.

## Web client

| Concern | Choice | Why |
|---|---|---|
| UI framework | **React 19** | Concurrent rendering, mature ecosystem, shared mental model with React Native |
| Language | **TypeScript** (strict) | Shared domain contracts between web, mobile and the API's DTOs |
| Build | **Vite 8** (Rolldown) | Sub-second HMR; source aliases let the app compile the shared packages from source |
| Routing | **React Router 7** (data router) | Nested layouts, route-level role guards |
| Styling | **Tailwind CSS 4** + CSS variables | Design tokens shared with mobile; light and dark themes through `--shell-*` variables |
| Accessible primitives | **Headless UI 2** | Dialogs, menus and listboxes with correct focus management |
| Icons | **lucide-react** | One icon family on web and mobile |
| Server state | **TanStack Query 5** | Caching, background refresh, cross-domain invalidation, polling |
| HTTP | **Axios** | Interceptors for token injection and 401 handling |
| Forms | **React Hook Form 7** + **Zod 4** | 18 forms validated by 14 schemas, with type inference from the schemas |
| Auth SDK | **Firebase JS SDK 12** (Auth only) | Email and password with verification, Google sign-in by popup or redirect |
| Lint | **ESLint 10** + typescript-eslint, react-hooks, react-refresh | |

## Mobile client

| Concern | Choice | Why |
|---|---|---|
| Runtime | **Expo SDK 54**, **React Native 0.81**, React 19.1 | Managed native toolchain, dev client, OTA-ready |
| Navigation | **React Navigation 7** (native stack + bottom tabs) | 6 role-conditional tabs, 37 stack screens |
| Styling | **NativeWind 4** (Tailwind 3 syntax) | The same token vocabulary as web |
| Animation and gestures | **Reanimated 4**, **Gesture Handler 2.28** | Pitch-board lineup interactions |
| Secure storage | **expo-secure-store** | Access token kept in the OS keychain or keystore |
| Federated sign-in | **expo-auth-session** | Google sign-in on device |
| Media | **expo-image-picker** | Player photos and compliance documents |
| Dev experience | Custom launcher with LAN IP detection, USB and tunnel modes | Reliable device testing on Windows networks |

## Shared packages (npm workspaces)

| Package | Contents |
|---|---|
| `shared-types` | About 165 exported domain types across 18 modules: the contract with the backend's DTOs |
| `shared-api` | Axios client factory, injectable token store, 17 per-domain API modules, error mapping |
| `shared-hooks` | About 149 TanStack Query hooks across 17 modules |
| `shared-stores` | Platform-agnostic `AuthProvider` and `ThemeProvider` (storage and navigation injected) |
| `shared-marketing` | Landing copy, role navigation and role dashboards used by both apps |
| `tailwind-config` | Shared design tokens |

## Backend

| Concern | Choice | Why |
|---|---|---|
| Runtime | **Java 21** (LTS) | Records, pattern matching, virtual-thread readiness |
| Framework | **Spring Boot 4.1** | Mature security, data and scheduling support |
| Web | Spring Web MVC + Bean Validation | Request DTO validation at the edge |
| Security | **Spring Security** + **JJWT** (HS256) | Stateless bearer tokens, method-level `@PreAuthorize` |
| Identity proofing | **Firebase Admin SDK 9** | Verifies Firebase ID tokens, using certificates cached in Secrets Manager |
| Persistence | **Spring Data JPA** / Hibernate | 53 entities, 53 repositories |
| Database | **PostgreSQL 16** | JSONB for rules and lineups, schema per bounded context, UUID keys |
| Scheduling | Spring `@Scheduled` | 4 operational cron jobs |
| Email | **AWS SDK v2: SES** (feature-flagged) | Transactional notifications alongside in-app ones |
| Secrets | **AWS SDK v2: Secrets Manager** | No secrets in images or configuration files |
| Boilerplate | Lombok | |
| Observability | Spring Boot Actuator | Liveness and readiness probes for the load balancer and container health checks |
| Tests | JUnit 5, Spring Boot Test, Spring Security Test, H2 (PostgreSQL mode) | Run in CI without a database server |

## Infrastructure and delivery

| Concern | Choice |
|---|---|
| Static hosting | S3 (private) + CloudFront with Origin Access Control |
| API compute | ECS Fargate in private subnets, behind an Application Load Balancer |
| Database | RDS for PostgreSQL 16, encrypted, deletion-protected |
| File storage | EFS access point mounted into the task |
| Egress strategy | **No NAT gateway**: interface VPC endpoints plus an S3 gateway endpoint |
| Secrets | AWS Secrets Manager; a scheduled Lambda refreshes Google's token-signing certificates |
| DNS and TLS | Route 53 + ACM |
| Monitoring and cost | CloudWatch alarms, AWS Budgets |
| Infrastructure as code | **Terraform** (AWS provider 6.x), with a `hibernate` cost toggle |
| Containers | Multi-stage Docker builds, non-root runtime user, health checks |
| CI/CD | **GitHub Actions** with **OIDC** role assumption (no long-lived cloud keys) |
| Local environment | Docker Compose: PostgreSQL + API, and an nginx-served web build |

## Explicitly *not* used (yet), and why

| Not used | Reason |
|---|---|
| Microservices | One team and one deployable; module boundaries are enforced in code instead ([ADR-0004](adr/0004-modular-monolith.md)) |
| WebSockets / SSE | 60-second polling meets current notification latency needs ([ADR-0007](adr/0007-polling-before-realtime.md)) |
| Redux / Zustand | Nearly all state is server state, which belongs in TanStack Query ([ADR-0008](adr/0008-tanstack-query-server-state.md)) |
| Turborepo / Nx | npm workspaces are enough at the current package count ([ADR-0001](adr/0001-npm-workspaces-shared-packages.md)) |

Prev: [02 — Architecture](02-architecture.md) · Next: [04 — Domain model](04-domain-model.md)
