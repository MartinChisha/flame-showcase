# ADR-0007: Polling before realtime for notifications

- **Status:** Accepted (revisit at scale)
- **Date:** 2026-08

## Context

Users receive in-app notifications: lineup reminders, referee gaps, overdue reports, approvals. These events are produced by scheduled jobs that run every 15–30 minutes or daily, and by user actions. None of them needs sub-second delivery.

## Decision

The notification inbox uses **TanStack Query polling every 60 seconds**. Other screens refresh on focus and after mutations through cache invalidation. Transactional email (AWS SES) is available behind a feature flag for out-of-app reach.

## Alternatives considered

| Option | Why not (yet) |
|---|---|
| WebSockets / STOMP | Sticky connections through the ALB, connection lifecycle on mobile, extra infrastructure for no user-visible gain |
| Server-Sent Events | Simpler than WebSockets, but the same infrastructure considerations |
| Push notifications (FCM / APNs) | Planned for mobile match-day alerts; independent of in-app delivery |

## Consequences

- Zero extra infrastructure; works through the existing stateless API.
- Worst-case notification latency is about 60 seconds, well within the domain's needs.
- Revisit when live match-day features (live scores, live event feeds) are introduced.
