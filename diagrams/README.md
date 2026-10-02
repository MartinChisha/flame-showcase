# Diagrams

Every diagram is a Mermaid source (`*.mmd`), so it can be reviewed in a pull request like code. GitHub renders the blocks below directly; pre-rendered SVGs live in [`svg/`](svg/).

CI re-renders every source with `@mermaid-js/mermaid-cli` on each push, so a broken diagram fails the build.

| # | Diagram | Shows | Discussed in |
|---|---|---|---|
| 01 | [System context](#01-system-context) | Who uses Flame and which external services it depends on. | [02-architecture.md](../docs/02-architecture.md) |
| 02 | [Containers](#02-containers) | Web and mobile clients, shared packages, the API and its data stores. | [02-architecture.md](../docs/02-architecture.md) |
| 03 | [Bounded-context map](#03-context-map) | The twelve backend modules and the direction of their dependencies. | [04-domain-model.md](../docs/04-domain-model.md) |
| 04 | [Identity token exchange](#04-auth-token-exchange) | Firebase proves identity; the API issues its own JWT and resolves roles per request. | [08-security-model.md](../docs/08-security-model.md) |
| 05 | [Request lifecycle](#05-request-lifecycle) | From a screen through TanStack Query, Axios, Spring Security and back. | [05-frontend-architecture.md](../docs/05-frontend-architecture.md) |
| 06 | [Season lifecycle](#06-season-lifecycle) | Draft to completed, with the readiness gate in front of activation. | [domains/02-competition.md](../docs/domains/02-competition.md) |
| 07 | [Policy resolution](#07-policy-resolution) | League pack, division and season bindings resolved into one effective rule set. | [domains/07-policy.md](../docs/domains/07-policy.md) |
| 08 | [Match-week readiness](#08-match-week-readiness) | Fixtures, referees and lineups converging before kickoff. | [domains/05-match.md](../docs/domains/05-match.md) |
| 09 | [Lineup lock decision](#09-lineup-lock) | One definition of "locked" shared by endpoints and the scheduler. | [domains/05-match.md](../docs/domains/05-match.md) |
| 10 | [Referee appointment and report](#10-referee-appointment-and-report) | Appointment, acceptance, the match report and its downstream effects. | [domains/06-referee.md](../docs/domains/06-referee.md) |
| 11 | [Discipline engine](#11-discipline-engine) | Cards in a report become suspensions and fines idempotently. | [domains/11-finance-discipline.md](../docs/domains/11-finance-discipline.md) |
| 12 | [Obligation lifecycle](#12-obligation-lifecycle) | Fees and fines from creation through payment, waiver or cancellation. | [domains/11-finance-discipline.md](../docs/domains/11-finance-discipline.md) |
| 13 | [AWS topology](#13-aws-topology) | Fargate in private subnets reaching AWS services through VPC endpoints, no NAT. | [07-cloud-and-devops.md](../docs/07-cloud-and-devops.md) |
| 14 | [CI/CD](#14-ci-cd) | GitHub Actions with OIDC for the API image and the static web build. | [07-cloud-and-devops.md](../docs/07-cloud-and-devops.md) |
| 15 | [Hibernate mode](#15-hibernate-mode) | One Terraform variable scales the platform to near-zero cost and back. | [07-cloud-and-devops.md](../docs/07-cloud-and-devops.md) |

<a id="01-system-context"></a>

## 01. System context

Who uses Flame and which external services it depends on.

```mermaid
flowchart TB
  subgraph people [People]
    Admin["Association and league admins"]
    Manager["Team managers"]
    Ref["Referees and branch admins"]
    Member["Members and the public"]
  end
  Flame["Flame platform - web, mobile, API"]
  Firebase["Firebase Authentication"]
  Email["AWS SES - transactional email"]
  Admin --> Flame
  Manager --> Flame
  Ref --> Flame
  Member --> Flame
  Flame -->|"verify ID tokens"| Firebase
  Flame -->|"optional notifications"| Email
```

Source: [`01-system-context.mmd`](01-system-context.mmd) · SVG: [`svg/01-system-context.svg`](svg/01-system-context.svg)

<a id="02-containers"></a>

## 02. Containers

Web and mobile clients, shared packages, the API and its data stores.

```mermaid
flowchart TB
  subgraph clients [Client applications]
    Web["Web SPA - React 19, Vite 8, Tailwind 4"]
    Mobile["Mobile - Expo 54, React Native 0.81"]
  end
  subgraph shared [Shared TypeScript packages]
    Types["shared-types"]
    Api["shared-api"]
    Hooks["shared-hooks"]
    Stores["shared-stores"]
    Marketing["shared-marketing"]
  end
  subgraph backend [API - modular monolith]
    Sec["Spring Security + JWT filter"]
    Spring["Spring Boot 4.1 / Java 21 - 12 domain modules"]
    Jobs["Scheduled jobs"]
    JPA["Spring Data JPA"]
  end
  DB[("PostgreSQL 16 - schema per context")]
  Files[("Upload storage")]
  Web --> Hooks
  Mobile --> Hooks
  Web --> Stores
  Mobile --> Stores
  Web --> Marketing
  Mobile --> Marketing
  Hooks --> Api
  Stores --> Api
  Api --> Types
  Api -->|"HTTPS, Bearer JWT"| Sec
  Sec --> Spring
  Spring --> JPA
  Jobs --> JPA
  JPA --> DB
  Spring --> Files
```

Source: [`02-containers.mmd`](02-containers.mmd) · SVG: [`svg/02-containers.svg`](svg/02-containers.svg)

<a id="03-context-map"></a>

## 03. Bounded-context map

The twelve backend modules and the direction of their dependencies.

```mermaid
flowchart TB
  Core["Core - identity"]
  League["League"]
  Team["Team"]
  Player["Player"]
  Competition["Competition"]
  Match["Match"]
  Referee["Referee"]
  Policy["Policy"]
  Ops["Operations"]
  Finance["Finance and Discipline"]
  Market["Marketplace"]
  Survey["Survey"]
  Core --> League
  Core --> Team
  Core --> Referee
  Core --> Market
  League --> Team
  League --> Competition
  Team --> Player
  Competition --> Match
  Team --> Match
  Player --> Match
  League --> Referee
  Competition --> Referee
  League --> Policy
  Competition --> Policy
  Player --> Policy
  Policy --> Finance
  Match --> Finance
  Competition --> Ops
  Match --> Ops
  Referee --> Ops
  Policy --> Ops
  Core -.-> Survey
```

Source: [`03-context-map.mmd`](03-context-map.mmd) · SVG: [`svg/03-context-map.svg`](svg/03-context-map.svg)

<a id="04-auth-token-exchange"></a>

## 04. Identity token exchange

Firebase proves identity; the API issues its own JWT and resolves roles per request.

```mermaid
sequenceDiagram
  participant U as User
  participant C as Web or Mobile
  participant FB as Firebase Auth
  participant API as Flame API
  participant SM as Secrets Manager
  U->>C: Sign in (verified email or Google)
  C->>FB: authenticate
  FB-->>C: Firebase ID token
  C->>API: exchange ID token
  API->>SM: read cached Google signing certs
  API->>API: verify signature, audience, issuer, expiry
  API->>API: find user by identity or provision
  API-->>C: Flame JWT (subject only)
  C->>C: persist via injected token store
  C->>API: GET current user
  API->>API: reload user and role from DB
  API-->>C: profile and role
  Note over C,API: Later requests carry the Flame JWT
  API-->>C: 401 when invalid or expired
  C->>C: clear token, route to login
```

Source: [`04-auth-token-exchange.mmd`](04-auth-token-exchange.mmd) · SVG: [`svg/04-auth-token-exchange.svg`](svg/04-auth-token-exchange.svg)

<a id="05-request-lifecycle"></a>

## 05. Request lifecycle

From a screen through TanStack Query, Axios, Spring Security and back.

```mermaid
sequenceDiagram
  participant UI as Screen
  participant Hook as shared-hooks
  participant Client as shared-api
  participant Filter as JwtAuthenticationFilter
  participant Ctrl as Controller
  participant Svc as Domain service
  participant DB as PostgreSQL
  UI->>Hook: useQuery or useMutation
  Hook->>Client: typed domain call
  Client->>Filter: HTTPS + Bearer token
  Filter->>Filter: verify, load user, map role
  Filter->>Ctrl: SecurityContext
  Ctrl->>Ctrl: PreAuthorize role rule
  Ctrl->>Svc: command or query DTO
  Svc->>Svc: row-level scope check
  Svc->>DB: repositories
  DB-->>Svc: entities
  Svc-->>UI: response DTO, cached by React Query
```

Source: [`05-request-lifecycle.mmd`](05-request-lifecycle.mmd) · SVG: [`svg/05-request-lifecycle.svg`](svg/05-request-lifecycle.svg)

<a id="06-season-lifecycle"></a>

## 06. Season lifecycle

Draft to completed, with the readiness gate in front of activation.

```mermaid
stateDiagram-v2
  [*] --> Draft: league admin creates season
  Draft --> FixturesGenerated: generate round-robin (>= 2 active teams)
  FixturesGenerated --> FixturesGenerated: regenerate or auto-schedule
  FixturesGenerated --> InProgress: start season
  note right of InProgress
    Server-side gate - policy readiness must pass
    for every team, including paid or waived
    participation fees
  end note
  InProgress --> Completed: all match weeks played
  Completed --> Archived
  Archived --> [*]
```

Source: [`06-season-lifecycle.mmd`](06-season-lifecycle.mmd) · SVG: [`svg/06-season-lifecycle.svg`](svg/06-season-lifecycle.svg)

<a id="07-policy-resolution"></a>

## 07. Policy resolution

League pack, division and season bindings resolved into one effective rule set.

```mermaid
flowchart LR
  subgraph authoring [Authoring]
    Draft["League policy pack - draft rules"]
    Publish["Publish - version n+1"]
    DivB["Division binding - inherit or override"]
    SeaB["Season binding - inherit or override"]
  end
  subgraph resolution [Resolution]
    Effective["Effective rule set for season"]
  end
  subgraph evaluation [Evaluation per team]
    Rules["Rule evaluators - squad size, contracts, documents, photos, deadlines, fees"]
    Result["Check results - met, unmet, waived, not applicable"]
    Cache["Team season readiness snapshot"]
  end
  Gate{"All blocking checks met?"}
  Start["Season may start"]
  Block["Start rejected with reasons"]
  Draft --> Publish --> Effective
  DivB --> Effective
  SeaB --> Effective
  Effective --> Rules --> Result --> Cache --> Gate
  Gate -->|yes| Start
  Gate -->|no| Block
```

Source: [`07-policy-resolution.mmd`](07-policy-resolution.mmd) · SVG: [`svg/07-policy-resolution.svg`](svg/07-policy-resolution.svg)

<a id="08-match-week-readiness"></a>

## 08. Match-week readiness

Fixtures, referees and lineups converging before kickoff.

```mermaid
sequenceDiagram
  participant LA as League admin
  participant UI as Match-week panel
  participant MWR as MatchWeekReadinessService
  participant CMP as Competition
  participant REF as Referee
  participant MAT as Match
  participant SET as Schedule settings
  LA->>UI: open season, select week 7
  UI->>MWR: readiness for week 7
  MWR->>SET: week window, deadlines
  MWR->>CMP: fixtures in week 7
  loop each fixture
    MWR->>CMP: date set, date inside window
    MWR->>REF: main referee appointed
    MWR->>MAT: home and away lineups submitted
    MWR->>MAT: report submitted and verified
  end
  MWR->>CMP: next week dated
  MWR-->>UI: checks per fixture, blockers, progress percent
  UI-->>LA: animated readiness bar, deep links to fix each blocker
```

Source: [`08-match-week-readiness.mmd`](08-match-week-readiness.mmd) · SVG: [`svg/08-match-week-readiness.svg`](svg/08-match-week-readiness.svg)

<a id="09-lineup-lock"></a>

## 09. Lineup lock decision

One definition of "locked" shared by endpoints and the scheduler.

```mermaid
flowchart TD
  Edit["Team manager saves or submits lineup"] --> L1{"Status already locked?"}
  L1 -->|yes| Reject["Reject - lineup locked"]
  L1 -->|no| L2{"Fixture already played?"}
  L2 -->|yes| Persist["Persist locked status"] --> Reject
  L2 -->|no| L3{"Kick-off passed?"}
  L3 -->|yes| Persist
  L3 -->|no| L4{"Submitted and inside deadline window before kick-off?"}
  L4 -->|yes| Persist
  L4 -->|no| Elig{"Any selected player suspended or blocked?"}
  Elig -->|yes| RejectP["Reject - list ineligible players"]
  Elig -->|no| Save["Save draft or submit"]
  Save --> Inv["Invalidate match-week readiness views"]
```

Source: [`09-lineup-lock.mmd`](09-lineup-lock.mmd) · SVG: [`svg/09-lineup-lock.svg`](svg/09-lineup-lock.svg)

<a id="10-referee-appointment-and-report"></a>

## 10. Referee appointment and report

Appointment, acceptance, the match report and its downstream effects.

```mermaid
sequenceDiagram
  participant BA as Branch admin
  participant RAS as RefereeAssignmentService
  participant ACC as Branch fee accruals
  participant N as NotificationService
  participant R as Referee
  participant MR as MatchReportService
  BA->>RAS: appoint referee as MAIN for fixture
  RAS->>RAS: verify branch serves fixture's league and division
  RAS->>RAS: replace any existing MAIN assignment
  RAS->>ACC: snapshot match fee for role
  RAS->>N: notify referee
  N-->>R: new appointment
  R->>R: acknowledge appointment
  Note over R: Match day
  R->>MR: create report, add events by minute
  R->>MR: submit report
  MR->>ACC: snapshot card fees as branch revenue
  MR-->>R: standings, statistics, discipline updated
```

Source: [`10-referee-appointment-and-report.mmd`](10-referee-appointment-and-report.mmd) · SVG: [`svg/10-referee-appointment-and-report.svg`](svg/10-referee-appointment-and-report.svg)

<a id="11-discipline-engine"></a>

## 11. Discipline engine

Cards in a report become suspensions and fines idempotently.

```mermaid
sequenceDiagram
  participant MR as MatchReportService
  participant DS as DisciplineService
  participant PR as Effective policy rules
  participant YT as Season yellow tally
  participant SAN as Player sanctions
  participant OS as ObligationService
  participant AUD as Finance audit log
  MR->>DS: report submitted with card events
  DS->>PR: discipline and fee rules for season
  loop each card event
    alt second yellow in same match and rule enabled
      DS->>SAN: red-card sanction (idempotent per event)
    else straight red
      DS->>SAN: red-card sanction, N matches incl. current
    else yellow
      DS->>YT: increment tally
      opt tally reaches threshold
        DS->>SAN: accumulation sanction
      end
    end
    DS->>OS: card fine billed to team or player (source ref = event)
    OS->>AUD: CREATE entry
  end
  Note over SAN: Covered fixtures consumed as matches pass, lineups reject active sanctions
```

Source: [`11-discipline-engine.mmd`](11-discipline-engine.mmd) · SVG: [`svg/11-discipline-engine.svg`](svg/11-discipline-engine.svg)

<a id="12-obligation-lifecycle"></a>

## 12. Obligation lifecycle

Fees and fines from creation through payment, waiver or cancellation.

```mermaid
stateDiagram-v2
  [*] --> Pending: created from policy, card or manual charge
  Pending --> Partial: payment less than outstanding
  Pending --> Paid: payment covers outstanding
  Pending --> Waived: admin waiver with reason
  Pending --> Overdue: past due date
  Partial --> Paid
  Partial --> Waived
  Partial --> Overdue
  Overdue --> Paid
  Overdue --> Waived
  Paid --> [*]
  Waived --> [*]
```

Source: [`12-obligation-lifecycle.mmd`](12-obligation-lifecycle.mmd) · SVG: [`svg/12-obligation-lifecycle.svg`](svg/12-obligation-lifecycle.svg)

<a id="13-aws-topology"></a>

## 13. AWS topology

Fargate in private subnets reaching AWS services through VPC endpoints, no NAT.

```mermaid
flowchart TB
  User["Browser or mobile app"]
  subgraph edge [Edge]
    R53["Route 53"]
    CF["CloudFront + ACM"]
    CFF["CloudFront Function - maintenance mode"]
  end
  subgraph vpc [VPC]
    subgraph publicNet [Public subnets]
      ALB["Application Load Balancer"]
    end
    subgraph privateNet [Private subnets - no NAT]
      ECS["ECS Fargate - API"]
      RDS[("RDS PostgreSQL 16")]
      EFS[("EFS uploads")]
      Jump["SSM jump host"]
      VPCE["Interface VPC endpoints"]
    end
    S3GW["S3 gateway endpoint"]
  end
  S3Web["S3 SPA bucket - private, OAC"]
  ECR["ECR"]
  SM["Secrets Manager"]
  CW["CloudWatch"]
  Lambda["Cert refresh Lambda"]
  User --> R53
  R53 --> CF
  CF --> S3Web
  CF -.-> CFF
  R53 --> ALB
  ALB --> ECS
  ECS --> RDS
  ECS --> EFS
  ECS --> VPCE
  ECS --> S3GW
  VPCE --> SM
  VPCE --> ECR
  VPCE --> CW
  Lambda --> SM
  Jump --> RDS
```

Source: [`13-aws-topology.mmd`](13-aws-topology.mmd) · SVG: [`svg/13-aws-topology.svg`](svg/13-aws-topology.svg)

<a id="14-ci-cd"></a>

## 14. CI/CD

GitHub Actions with OIDC for the API image and the static web build.

```mermaid
flowchart LR
  subgraph api [API pipeline]
    A1["PR or push"] --> A2["mvn test"]
    A2 -->|"main"| A3["OIDC deploy role"]
    A3 --> A4{"Image for SHA in ECR?"}
    A4 -->|no| A5["docker build and push"]
    A4 -->|yes| A6["Render task definition"]
    A5 --> A6
    A6 --> A7["Deploy ECS service, wait for stability"]
  end
  subgraph web [Web pipeline]
    W1["Change in web or shared packages"] --> W2["npm ci + typecheck"]
    W2 --> W3["Vite build"]
    W3 -->|"main"| W4["OIDC deploy role"]
    W4 --> W5["S3 sync"]
    W5 --> W6["CloudFront invalidation"]
  end
```

Source: [`14-ci-cd.mmd`](14-ci-cd.mmd) · SVG: [`svg/14-ci-cd.svg`](svg/14-ci-cd.svg)

<a id="15-hibernate-mode"></a>

## 15. Hibernate mode

One Terraform variable scales the platform to near-zero cost and back.

```mermaid
stateDiagram-v2
  [*] --> Live
  Live --> Hibernated: terraform apply hibernate=true
  Hibernated --> Live: terraform apply hibernate=false + restore snapshot
  state Live {
    [*] --> Serving
    Serving: ALB, ECS task, RDS, VPC endpoints, alarms, jump host
  }
  state Hibernated {
    [*] --> Parked
    Parked: ECS at 0, RDS final snapshot, maintenance function attached, marketing pages still served
  }
```

Source: [`15-hibernate-mode.mmd`](15-hibernate-mode.mmd) · SVG: [`svg/15-hibernate-mode.svg`](svg/15-hibernate-mode.svg)
