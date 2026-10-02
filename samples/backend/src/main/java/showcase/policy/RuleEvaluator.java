package showcase.policy;

import java.util.Map;
import showcase.policy.Readiness.CheckResult;
import showcase.policy.Readiness.CheckStatus;
import showcase.policy.Readiness.Rule;
import showcase.policy.Readiness.RuleKey;
import showcase.policy.Readiness.TeamSnapshot;

/**
 * Adding a rule type means adding an enum value and one evaluator. Parameters are validated when a
 * league admin saves a policy pack, never lazily during evaluation.
 */
public interface RuleEvaluator {

  RuleKey key();

  /** Throws {@link IllegalArgumentException} with a user-facing message for malformed params. */
  void validate(Map<String, Object> params);

  CheckResult evaluate(Rule rule, TeamSnapshot team);

  default CheckResult result(Rule rule, boolean met, String detail) {
    return new CheckResult(rule.key(), rule.severity(), met ? CheckStatus.MET : CheckStatus.UNMET, detail);
  }

  static int positiveInt(Map<String, Object> params, String name) {
    Object value = params.get(name);
    if (!(value instanceof Number number) || number.intValue() < 1 || number.doubleValue() % 1 != 0) {
      throw new IllegalArgumentException("'" + name + "' must be a whole number of at least 1");
    }
    return number.intValue();
  }
}
