# Backend samples

Pure Java 21 domain logic, compiled and tested with nothing but a JDK (`./run-tests.sh`). The production versions sit inside Spring services; extracting the rules into plain, clock-injected classes is the pattern being shown.

| Sample | Real-world context |
|---|---|
| `competition/RoundRobinScheduler` | Generates a season's fixtures when a league admin presses *Generate*. Tests prove every pair meets once per leg, nobody is double-booked, odd team counts get byes, the second leg mirrors the first, and home and away games are balanced. |
| `policy/ReadinessEngine` + evaluators | Decides whether a season may start. League rules are resolved through division and season bindings, then each team is evaluated. Waivers and not-applicable checks are first-class, and parameters are validated when an admin saves a pack. |
| `match/LineupLockPolicy` | One definition of "locked", shared by the save endpoint, the submit endpoint and the scheduler. It returns a sealed `Decision` so callers cannot forget a case. |
| `finance/SuspensionCalculator` | Runs when a referee submits a match report. Bans include the current match, a double yellow counts as a red, and accumulation bans start next fixture. Deterministic source references make reprocessing safe. |

## Module layout in production

```text
<module>/
  controller/   REST endpoints, @PreAuthorize role rules, request validation
  service/      business rules, resource scoping, cross-module orchestration
  repository/   Spring Data JPA interfaces
  entity/       JPA entities mapped to the module's PostgreSQL schema
  dto/          request / response records
  enums/        statuses, rule keys, check keys
```

[`illustrative/`](illustrative/) shows these conventions with Spring annotations. Those files are for reading only and are not compiled.
