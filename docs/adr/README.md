# Architecture Decision Records

Each record captures the context, the decision, the alternatives that were considered, and the consequences. The format follows Michael Nygard's lightweight ADR template.

| # | Decision | Status |
|---|---|---|
| [0001](0001-npm-workspaces-shared-packages.md) | npm workspaces monorepo with shared domain packages | Accepted |
| [0002](0002-platform-agnostic-stores-di.md) | Platform-agnostic stores through dependency injection | Accepted |
| [0003](0003-firebase-identity-backend-jwt.md) | Firebase for identity, Flame-issued JWT for authorisation | Accepted |
| [0004](0004-modular-monolith.md) | Modular monolith instead of microservices | Accepted |
| [0005](0005-ecs-fargate-without-nat.md) | ECS Fargate in private subnets without a NAT gateway | Accepted |
| [0006](0006-terraform-iac-and-hibernate.md) | Terraform as the source of truth, with a hibernate toggle | Accepted |
| [0007](0007-polling-before-realtime.md) | Polling before realtime for notifications | Accepted (revisit at scale) |
| [0008](0008-tanstack-query-server-state.md) | TanStack Query as the server-state layer | Accepted |
| [0009](0009-policy-rules-as-jsonb.md) | Policy rules as versioned JSONB with hierarchical bindings | Accepted |
| [0010](0010-capability-based-sub-roles.md) | Capability-based sub-roles instead of new role values | Accepted |
