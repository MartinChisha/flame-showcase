# ADR-0004: Modular monolith instead of microservices

- **Status:** Accepted
- **Date:** 2026-05

## Context

The domain has 12 bounded contexts with heavy interaction. Submitting one match report touches match, competition, policy, finance and operations. The team is small, there is one production environment, and transactional consistency matters: a report must not update standings without also applying the suspensions it causes.

## Decision

Build a **single Spring Boot deployable** organised as bounded-context modules:

- one Java package per context, each with the same `controller / service / repository / entity / dto / enums` layering
- one **PostgreSQL schema per context**, keeping table ownership visible
- cross-context workflows owned by explicit **orchestrating services** (report submission, policy readiness, match-week readiness) instead of ad-hoc calls scattered through the code
- entities never leave the service layer; controllers speak DTOs

## Alternatives considered

| Option | Why not |
|---|---|
| Microservices per context | Distributed transactions or sagas for every report submission; much higher operational cost for one team |
| Layered monolith (one big service layer) | Fast to start, but boundaries erode; the schema-per-context rule and per-module layering prevent this |

## Consequences

- ACID transactions across contexts where the domain needs them.
- One container, one pipeline, one database to operate.
- Context boundaries are a convention plus schema separation, not a network boundary. Extracting a context later (marketplace and survey are the most autonomous) is possible because their dependencies already point one way.
