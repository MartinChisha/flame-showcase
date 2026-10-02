package showcase.policy;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Value types for the policy readiness engine. */
public final class Readiness {

  private Readiness() {}

  public enum RuleKey {
    TEAM_MIN_REGISTERED_CONTRACTS,
    TEAM_MAX_SQUAD_SIZE,
    PLAYER_PHOTO_REQUIRED,
    PLAYER_DOCUMENT_REQUIRED,
    TEAM_PARTICIPATION_FEE_PAID
  }

  public enum Severity {
    BLOCKING,
    WARNING
  }

  public enum CheckStatus {
    MET,
    UNMET,
    WAIVED,
    NOT_APPLICABLE
  }

  /** One configured rule, as stored in a policy pack or binding (JSONB in production). */
  public record Rule(RuleKey key, Severity severity, Map<String, Object> params) {
    public Rule {
      params = Map.copyOf(params);
    }
  }

  public record PlayerSnapshot(
      String playerId, boolean contractRegistered, boolean hasPhoto, Set<String> approvedDocuments) {
    public PlayerSnapshot {
      approvedDocuments = Set.copyOf(approvedDocuments);
    }
  }

  /** Everything the engine needs about one team, loaded once per evaluation. */
  public record TeamSnapshot(
      String teamId,
      List<PlayerSnapshot> squad,
      boolean participationFeeSettled,
      Set<RuleKey> waivedRules) {
    public TeamSnapshot {
      squad = List.copyOf(squad);
      waivedRules = Set.copyOf(waivedRules);
    }
  }

  public record CheckResult(RuleKey key, Severity severity, CheckStatus status, String detail) {}

  public record TeamReadiness(String teamId, List<CheckResult> checks) {
    public TeamReadiness {
      checks = List.copyOf(checks);
    }

    /** Ready when no blocking check is unmet. Waived and not-applicable checks pass. */
    public boolean ready() {
      return checks.stream()
          .noneMatch(c -> c.severity() == Severity.BLOCKING && c.status() == CheckStatus.UNMET);
    }

    public int progressPercent() {
      List<CheckResult> blocking =
          checks.stream().filter(c -> c.severity() == Severity.BLOCKING).toList();
      if (blocking.isEmpty()) return 100;
      long passed = blocking.stream().filter(c -> c.status() != CheckStatus.UNMET).count();
      return (int) Math.round(100.0 * passed / blocking.size());
    }
  }
}
