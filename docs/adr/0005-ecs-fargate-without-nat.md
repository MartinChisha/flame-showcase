# ADR-0005: ECS Fargate in private subnets without a NAT gateway

- **Status:** Accepted
- **Date:** 2026-09

## Context

The API container must run in private subnets. It needs to reach Secrets Manager, ECR, CloudWatch Logs, S3 and, for Firebase token verification, Google's public signing certificates. A NAT gateway is the conventional answer, but it adds a fixed hourly cost and per-GB charges, and it opens a general internet egress path from the workload.

## Decision

- Run the API on **ECS Fargate** in private subnets, behind an ALB in public subnets.
- Provide AWS service access through **interface VPC endpoints** and a free **S3 gateway endpoint**.
- Remove the only internet dependency, Google's certificates, by having a **scheduled Lambda** (outside the VPC) copy them into Secrets Manager every six hours. The API reads them through its endpoint, and an HTTP fallback is disabled by default.

## Alternatives considered

| Option | Why not |
|---|---|
| NAT gateway | Cost, plus broader egress than the workload needs |
| Public subnets with public IPs | Exposes tasks directly; weaker posture |
| EC2 / Elastic Beanstalk | More patching and capacity management for a single small service |

## Consequences

- No general internet egress from the API: a smaller attack surface and data-exfiltration path.
- Interface endpoints become the dominant fixed cost while live. They are therefore removed in [hibernate mode](0006-terraform-iac-and-hibernate.md).
- Any new external dependency has to be designed explicitly (relay, endpoint or Lambda), which is a deliberate speed bump.
