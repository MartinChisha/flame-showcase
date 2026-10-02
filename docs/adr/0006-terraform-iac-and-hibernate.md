# ADR-0006: Terraform as the source of truth, with a hibernate toggle

- **Status:** Accepted
- **Date:** 2026-10

## Context

The production environment was first assembled by hand in the console. That worked, but it was undocumented, hard to reproduce in another account, and risky to change. The platform also has seasonal usage, with long quiet periods between competitions, where paying for full production capacity is wasteful.

## Decision

1. **Adopt the live stack into Terraform** using `import` blocks, iterating until `terraform plan` reports zero changes. Terraform then becomes the only way production changes.
2. Add a boolean **`hibernate`** variable. When `true`:
   - ECS desired count goes to 0; the ALB, interface endpoints, alarms and jump host are removed (`count = var.hibernate ? 0 : 1`)
   - RDS is destroyed **after a named final snapshot**
   - a CloudFront Function is attached that serves a branded maintenance page for app routes, while marketing pages stay online
3. Resuming sets `hibernate = false` and restores RDS from the snapshot **with the same identifier**, so connection strings in Secrets Manager stay valid.
4. Keep a generated CloudFormation template as a portable backup, and an encrypted off-cloud backup bundle for account-loss scenarios.

## Consequences

- Production is reproducible and reviewable as code; the disaster-recovery runbook is mostly `terraform apply`.
- Idle cost drops to a few dollars a month without losing data or DNS.
- Hibernating needs a short, documented sequence: pause the deploy workflow, lift deletion protection, then apply. A forgotten step fails safely.
- State must be protected: local state is git-ignored and backed up, and a remote backend is prepared.

See the sanitised outline in [`samples/infra/terraform`](../../samples/infra/terraform/).
