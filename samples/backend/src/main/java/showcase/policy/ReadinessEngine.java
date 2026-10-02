package showcase.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import showcase.policy.Readiness.CheckResult;
import showcase.policy.Readiness.CheckStatus;
import showcase.policy.Readiness.Rule;
import showcase.policy.Readiness.RuleKey;
import showcase.policy.Readiness.TeamReadiness;
import showcase.policy.Readiness.TeamSnapshot;

/**
 * Resolves a league's published rules through division and season bindings, then evaluates the
 * effective rule set for a team.
 */
public final class ReadinessEngine {

  /** A binding layer: overrides replace a rule by key, disabled keys remove it. */
  public record Binding(Collection<Rule> overrides, Set<RuleKey> disabled) {
    public static final Binding NONE = new Binding(List.of(), Set.of());

    public Binding {
      overrides = List.copyOf(overrides);
      disabled = Set.copyOf(disabled);
    }
  }

  private final Map<RuleKey, RuleEvaluator> evaluators = new EnumMap<>(RuleKey.class);

  public ReadinessEngine(Collection<RuleEvaluator> evaluators) {
    for (RuleEvaluator e : evaluators) {
      if (this.evaluators.put(e.key(), e) != null) {
        throw new IllegalStateException("duplicate evaluator for " + e.key());
      }
    }
  }

  /** Called on every policy save so invalid packs never reach evaluation. */
  public void validate(Collection<Rule> rules) {
    for (Rule rule : rules) {
      evaluatorFor(rule.key()).validate(rule.params());
    }
  }

  /** League, then division, then season: the most specific layer wins. */
  public static List<Rule> resolve(Collection<Rule> leaguePublished, Binding division, Binding season) {
    Map<RuleKey, Rule> effective = new EnumMap<>(RuleKey.class);
    leaguePublished.forEach(r -> effective.put(r.key(), r));
    for (Binding layer : List.of(division, season)) {
      layer.overrides().forEach(r -> effective.put(r.key(), r));
      layer.disabled().forEach(effective::remove);
    }
    return List.copyOf(effective.values());
  }

  public TeamReadiness evaluate(Collection<Rule> effectiveRules, TeamSnapshot team) {
    List<CheckResult> checks = new ArrayList<>(effectiveRules.size());
    for (Rule rule : effectiveRules) {
      if (team.waivedRules().contains(rule.key())) {
        checks.add(new CheckResult(rule.key(), rule.severity(), CheckStatus.WAIVED, "waived by league admin"));
      } else if (team.squad().isEmpty() && rule.key().name().startsWith("PLAYER_")) {
        checks.add(new CheckResult(rule.key(), rule.severity(), CheckStatus.NOT_APPLICABLE, "no registered players yet"));
      } else {
        checks.add(evaluatorFor(rule.key()).evaluate(rule, team));
      }
    }
    return new TeamReadiness(team.teamId(), checks);
  }

  private RuleEvaluator evaluatorFor(RuleKey key) {
    RuleEvaluator evaluator = evaluators.get(key);
    if (evaluator == null) throw new IllegalArgumentException("no evaluator registered for " + key);
    return evaluator;
  }
}
