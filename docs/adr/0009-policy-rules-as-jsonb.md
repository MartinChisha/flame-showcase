# ADR-0009: Policy rules as versioned JSONB with hierarchical bindings

- **Status:** Accepted
- **Date:** 2026-07

## Context

Every league has different rules: squad caps, minimum registered players, required documents, photo requirements, registration deadlines, participation fees, card fines and suspension lengths. Divisions within a league (youth vs senior, for example) and individual seasons sometimes need overrides. New rule types appear regularly as associations ask for them.

## Decision

- Each league has one **policy pack** with `draft` and `published` rule sets, stored as **JSONB**, plus a published version number. Publishing increments the version, and an empty pack cannot be published.
- **Bindings** at division and season level store only overrides; the effective rule set is resolved as league, then division, then season.
- Each rule is `{ ruleKey, severity, params }`. A **parameter validator** for each rule key rejects malformed parameters when a pack is saved, not when it is evaluated.
- Evaluators are registered per rule key and produce `met`, `unmet`, `waived` or `not_applicable`. Readiness is the conjunction of all **blocking** checks.
- Enforcement lives in other contexts: competition (season start), match (lineups) and finance (fees and sanctions). They consume the resolved rules and never redefine amounts.

## Consequences

- New rule types are a new enum value, a validator and an evaluator, with **no schema migration**.
- League admins can configure complex rules through a wizard with presets, without developer involvement.
- JSONB needs disciplined validation at the edge; the validator is mandatory on every write path.
- Readiness snapshots are cached per team and season and recalculated on demand to keep dashboards fast.

A clean-room version of the rule-evaluation pattern is in [`samples/backend`](../../samples/backend/).
