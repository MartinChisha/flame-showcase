package showcase.policy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import showcase.policy.Readiness.CheckResult;
import showcase.policy.Readiness.PlayerSnapshot;
import showcase.policy.Readiness.Rule;
import showcase.policy.Readiness.RuleKey;
import showcase.policy.Readiness.TeamSnapshot;

/** A representative subset of rule evaluators. */
public final class Evaluators {

  private Evaluators() {}

  public static List<RuleEvaluator> defaults() {
    return List.of(
        new MinRegisteredContracts(), new MaxSquadSize(), new PhotoRequired(),
        new DocumentsRequired(), new ParticipationFeePaid());
  }

  static final class MinRegisteredContracts implements RuleEvaluator {
    public RuleKey key() { return RuleKey.TEAM_MIN_REGISTERED_CONTRACTS; }

    public void validate(Map<String, Object> params) { RuleEvaluator.positiveInt(params, "min"); }

    public CheckResult evaluate(Rule rule, TeamSnapshot team) {
      int min = RuleEvaluator.positiveInt(rule.params(), "min");
      long registered = team.squad().stream().filter(PlayerSnapshot::contractRegistered).count();
      return result(rule, registered >= min, registered + " of " + min + " required contracts registered");
    }
  }

  static final class MaxSquadSize implements RuleEvaluator {
    public RuleKey key() { return RuleKey.TEAM_MAX_SQUAD_SIZE; }

    public void validate(Map<String, Object> params) { RuleEvaluator.positiveInt(params, "max"); }

    public CheckResult evaluate(Rule rule, TeamSnapshot team) {
      int max = RuleEvaluator.positiveInt(rule.params(), "max");
      int size = team.squad().size();
      return result(rule, size <= max, size + " players (limit " + max + ")");
    }
  }

  static final class PhotoRequired implements RuleEvaluator {
    public RuleKey key() { return RuleKey.PLAYER_PHOTO_REQUIRED; }

    public void validate(Map<String, Object> params) {}

    public CheckResult evaluate(Rule rule, TeamSnapshot team) {
      long missing = team.squad().stream().filter(p -> !p.hasPhoto()).count();
      return result(rule, missing == 0, missing == 0 ? "all players have photos" : missing + " player(s) missing a photo");
    }
  }

  static final class DocumentsRequired implements RuleEvaluator {
    public RuleKey key() { return RuleKey.PLAYER_DOCUMENT_REQUIRED; }

    public void validate(Map<String, Object> params) {
      if (!(params.get("documentTypes") instanceof Collection<?> types) || types.isEmpty()) {
        throw new IllegalArgumentException("'documentTypes' must list at least one document type");
      }
    }

    public CheckResult evaluate(Rule rule, TeamSnapshot team) {
      Set<String> required = Set.copyOf(((Collection<?>) rule.params().get("documentTypes"))
          .stream().map(String::valueOf).toList());
      long incomplete = team.squad().stream()
          .filter(p -> !p.approvedDocuments().containsAll(required)).count();
      return result(rule, incomplete == 0,
          incomplete == 0 ? "all required documents approved" : incomplete + " player(s) with outstanding documents");
    }
  }

  static final class ParticipationFeePaid implements RuleEvaluator {
    public RuleKey key() { return RuleKey.TEAM_PARTICIPATION_FEE_PAID; }

    public void validate(Map<String, Object> params) {}

    public CheckResult evaluate(Rule rule, TeamSnapshot team) {
      return result(rule, team.participationFeeSettled(),
          team.participationFeeSettled() ? "participation fee paid or waived" : "participation fee outstanding");
    }
  }
}
