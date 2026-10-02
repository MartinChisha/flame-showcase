# ADR-0001: npm workspaces monorepo with shared domain packages

- **Status:** Accepted
- **Date:** 2026-06

## Context

Flame ships a web app and a mobile app for the same roles and the same workflows. The first web prototype held its types, API calls and hooks inside the app, and starting the mobile app would have duplicated all of it. The team is small, so duplicated logic would quickly drift: one platform would get a validation fix and the other would not.

## Decision

Use a single **npm workspaces** monorepo with two apps (`web`, `mobile`) and internal packages for everything that is not UI: `shared-types`, `shared-api`, `shared-hooks`, `shared-stores`, `shared-marketing` and `tailwind-config`. The web app's bundler aliases the packages to their TypeScript source, so changes show up instantly without a build step.

## Alternatives considered

| Option | Why not (for now) |
|---|---|
| Turborepo / Nx | Task caching and graph tooling are valuable at larger package counts; with 6 packages and 2 apps they add configuration without a measurable win |
| pnpm workspaces | Stricter dependency isolation clashes with React Native's Metro resolver, which assumes hoisting, and needs extra configuration |
| Publishing packages to a registry | Versioning overhead for packages that only have two consumers living in the same repository |

## Consequences

- **About 149 hooks and about 165 types are written once** and consumed by both platforms.
- An API contract change is one pull request across types, transport, hooks and both UIs.
- Root `overrides` are needed to pin the React and React Native versions that Expo requires, separately from the web app's React version.
- Moving to Turborepo later is incremental: the workspace layout is already compatible.
