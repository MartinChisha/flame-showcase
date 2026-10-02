# ADR-0003: Firebase for identity, Flame-issued JWT for authorisation

- **Status:** Accepted
- **Date:** 2026-08

## Context

The first release used email and password login implemented in the API (BCrypt hashes, a JWT on success). Users then asked for Google sign-in and verified email addresses. Building federated login, verification email flows and password reset in-house is expensive and security-sensitive. At the same time, **authorisation** (roles, league scoping, capabilities) is core domain logic and must stay inside Flame.

## Decision

- Use **Firebase Authentication** for identity proofing only: email and password with mandatory verification, plus Google.
- Clients exchange the Firebase ID token for a **Flame-issued JWT** at a single API endpoint. The API verifies the ID token (signature, audience, issuer, expiry) and then finds or provisions the user.
- The Flame JWT carries **only the subject**. Roles are loaded from the database on every request.
- Google's token-signing certificates are read from Secrets Manager (refreshed by a scheduled Lambda), so the private API needs no internet egress ([ADR-0005](0005-ecs-fargate-without-nat.md)).

## Alternatives considered

| Option | Why not |
|---|---|
| Use Firebase tokens directly as API credentials | Couples every request to Google key rotation, and Firebase custom claims are a poor fit for league-scoped, assignment-based permissions |
| Amazon Cognito | Viable, but Firebase's client SDKs for web and Expo were more mature for the target users |
| Keep everything in-house | Highest effort and risk for no differentiation |

## Consequences

- Role changes and revocations take effect on the next request; there are no stale role claims.
- One database read per request for the user. This is acceptable at current scale and cacheable later.
- Legacy password login stays available alongside the Firebase flow while existing accounts migrate.
- Identity is portable: Flame owns the user table, and Firebase is a replaceable identity provider.
