# Infrastructure samples

A sanitised outline of the production patterns. Every account number, ARN, bucket and hostname is a placeholder. **Nothing here is ever applied.**

| Sample | What it demonstrates |
|---|---|
| [`terraform/main.tf`](terraform/main.tf) | `hibernate` toggle: endpoints, ALB and RDS are created or destroyed with `count` / `for_each`. RDS keeps its identifier across a snapshot restore. |
| [`terraform/ecs.tf`](terraform/ecs.tf) | Fargate in private subnets, secrets injected from Secrets Manager, deployment circuit breaker, task revisions owned by CI |
| [`terraform/web.tf`](terraform/web.tf) + [`maintenance.js`](terraform/maintenance.js) | Private S3 + OAC, and a CloudFront Function attached only while hibernated |
| [`terraform/iam_github_oidc.tf`](terraform/iam_github_oidc.tf) | GitHub OIDC trust limited to one repository's `main` branch, with a least-privilege deploy policy |
| [`github-actions/deploy-api.example.yml`](github-actions/deploy-api.example.yml) | Test, then build once per commit, render the live task definition, and deploy while waiting for stability |
| [`docker/Dockerfile.api`](docker/Dockerfile.api) | Multi-stage build, non-root runtime, container health check |

The production stack also covers VPC and subnets, security-group chaining, EFS, Route 53 and ACM, CloudWatch alarms, budgets, an SSM jump host and the certificate-refresh Lambda. See [07 — Cloud and DevOps](../../docs/07-cloud-and-devops.md).
