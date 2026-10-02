# 02 — System Architecture

Flame is a **three-tier system**. Two client applications (web and mobile) share their business logic through internal TypeScript packages. Both talk to a single Spring Boot API over HTTPS with bearer tokens, and the API owns a PostgreSQL database. Firebase Authentication handles identity proofing; Flame's own API owns authorisation.

## System context

```mermaid
flowchart TB
  subgraph people [People]
    Admin["Association and league admins"]
    Manager["Team managers"]
    Ref["Referees and branch admins"]
    Member["Members and the public"]
  end
  Flame["Flame platform"]
  Firebase["Firebase Authentication"]
  Email["AWS SES (transactional email)"]
  Admin --> Flame
  Manager --> Flame
  Ref --> Flame
  Member --> Flame
  Flame -->|"verify ID tokens"| Firebase
  Flame -->|"optional notifications"| Email
```

## Containers

```mermaid
flowchart TB
  subgraph clients [Client applications]
    Web["Web SPA - React 19, Vite 8, Tailwind 4"]
    Mobile["Mobile app - Expo 54, React Native 0.81, NativeWind"]
  end
  subgraph shared [Shared TypeScript packages]
    Types["shared-types - domain contracts"]
    Api["shared-api - Axios client + 17 domain modules"]
    Hooks["shared-hooks - ~149 TanStack Query hooks"]
    Stores["shared-stores - Auth + Theme providers"]
    Marketing["shared-marketing - nav, dashboards, copy"]
  end
  subgraph backend [API - modular monolith]
    Spring["Spring Boot 4.1 / Java 21"]
    Sec["Spring Security + JWT filter"]
    Jobs["Scheduled jobs"]
    JPA["Spring Data JPA"]
  end
  DB[("PostgreSQL 16 - schema per bounded context")]
  Files[("Upload storage - EFS")]
  Web --> Hooks
  Mobile --> Hooks
  Hooks --> Api
  Api --> Types
  Stores --> Api
  Web --> Marketing
  Mobile --> Marketing
  Api -->|"HTTPS, Bearer JWT"| Sec --> Spring
  Spring --> JPA --> DB
  Jobs --> JPA
  Spring --> Files
```

## Request lifecycle

```mermaid
sequenceDiagram
  participant UI as Screen
  participant Hook as shared-hooks
  participant Client as shared-api
  participant Filter as JwtAuthenticationFilter
  participant Ctrl as Controller
  participant Svc as Domain service
  participant DB as PostgreSQL

  UI->>Hook: useQuery / useMutation
  Hook->>Client: typed domain call
  Client->>Filter: HTTPS + Authorization Bearer
  Filter->>Filter: verify signature, load user, map role to authority
  Filter->>Ctrl: SecurityContext
  Ctrl->>Ctrl: @PreAuthorize role check
  Ctrl->>Svc: command / query
  Svc->>Svc: row-level scope checks (e.g. admin of this league)
  Svc->>DB: repositories across schemas
  DB-->>UI: JSON DTO, cached by React Query
```

1. A screen calls a React Query hook. The **same hook** runs on web and mobile.
2. The hook calls a typed function in the shared API package.
3. The Axios client attaches the bearer token kept in memory.
4. `JwtAuthenticationFilter` verifies the token and loads the user **from the database on every request**, so role changes take effect immediately.
5. `@PreAuthorize` enforces role access on the controller method. The service then applies **row-level** scope (for example, "is this user an admin of *this* league?").
6. The response is a DTO, never an entity. React Query caches it and invalidates related keys after mutations.

## Authentication: Firebase identity, Flame authorisation

```mermaid
sequenceDiagram
  participant U as User
  participant C as Web or Mobile
  participant FB as Firebase Auth
  participant API as Flame API
  participant SM as Secrets Manager

  U->>C: Sign in (email + verification, or Google)
  C->>FB: authenticate
  FB-->>C: Firebase ID token
  C->>API: exchange ID token
  API->>SM: cached Google signing certificates
  API->>API: verify token, find or provision user
  API-->>C: Flame access token (JWT)
  C->>C: store token (session storage / secure store)
  C->>API: GET current user (hydrate session)
  Note over C,API: Every later request carries the Flame JWT
  API-->>C: 401
  C->>C: clear token, route to login
```

Why this split: Firebase handles password storage, email verification and federated sign-in, which are expensive and risky to build in-house. Flame keeps **authorisation**, roles and data access completely inside its own API. See [ADR-0003](adr/0003-firebase-identity-backend-jwt.md) and [08 — Security model](08-security-model.md).

## Cross-domain cache invalidation

Readiness is derived from many other contexts, so the frontend invalidates related data when one of them changes:

| Mutation | Invalidated views |
|---|---|
| Player, contract or document change | Season readiness, team readiness |
| Lineup submit, fixture reschedule, referee appointment | Match-week readiness, fixture readiness, week list |
| Match report submit | All of the above, plus standings, player stats, scouting rankings |
| Season status change | Season and readiness views |

## Deployment view

```mermaid
flowchart LR
  Dev["Developer push to main"] --> GHA["GitHub Actions"]
  GHA -->|"OIDC role"| AWS
  subgraph AWS [AWS - single region]
    S3["Private S3 bucket"] --> CF["CloudFront (OAC)"]
    ECR["ECR"] --> ECS["ECS Fargate service (private subnets)"]
    ALB["ALB (HTTPS)"] --> ECS
    ECS --> RDS[("RDS PostgreSQL 16")]
    ECS --> EFS[("EFS uploads")]
    ECS --> VPCE["VPC endpoints (no NAT)"]
    Lambda["Cert refresh Lambda"] --> SM["Secrets Manager"]
    VPCE --> SM
  end
```

Detail in [07 — Cloud and DevOps](07-cloud-and-devops.md).

Prev: [01 — Product overview](01-product-overview.md) · Next: [03 — Tech stack](03-tech-stack.md)
