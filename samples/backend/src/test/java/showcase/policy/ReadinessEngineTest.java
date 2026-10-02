package showcase.policy;

import static showcase.TestRunner.check;
import static showcase.TestRunner.equal;
import static showcase.TestRunner.throwsIllegalArgument;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import showcase.policy.Readiness.CheckResult;
import showcase.policy.Readiness.CheckStatus;
import showcase.policy.Readiness.PlayerSnapshot;
import showcase.policy.Readiness.Rule;
import showcase.policy.Readiness.RuleKey;
import showcase.policy.Readiness.Severity;
import showcase.policy.Readiness.TeamReadiness;
import showcase.policy.Readiness.TeamSnapshot;
import showcase.policy.ReadinessEngine.Binding;

public final class ReadinessEngineTest {

  private static final ReadinessEngine ENGINE = new ReadinessEngine(Evaluators.defaults());

  private static final List<Rule> LEAGUE = List.of(
      new Rule(RuleKey.TEAM_MIN_REGISTERED_CONTRACTS, Severity.BLOCKING, Map.of("min", 16)),
      new Rule(RuleKey.TEAM_MAX_SQUAD_SIZE, Severity.BLOCKING, Map.of("max", 30)),
      new Rule(RuleKey.PLAYER_PHOTO_REQUIRED, Severity.WARNING, Map.of()),
      new Rule(RuleKey.PLAYER_DOCUMENT_REQUIRED, Severity.BLOCKING, Map.of("documentTypes", List.of("ID", "MEDICAL"))),
      new Rule(RuleKey.TEAM_PARTICIPATION_FEE_PAID, Severity.BLOCKING, Map.of()));

  private static TeamSnapshot team(int players, boolean feePaid, Set<RuleKey> waivers) {
    List<PlayerSnapshot> squad = IntStream.range(0, players)
        .mapToObj(i -> new PlayerSnapshot("P" + i, true, i % 5 != 0, Set.of("ID", "MEDICAL")))
        .toList();
    return new TeamSnapshot("riverside-fc", squad, feePaid, waivers);
  }

  private static CheckStatus status(TeamReadiness r, RuleKey key) {
    return r.checks().stream().filter(c -> c.key() == key).map(CheckResult::status).findFirst().orElseThrow();
  }

  public static void testCompliantTeamIsReadyDespiteWarnings() {
    TeamReadiness r = ENGINE.evaluate(LEAGUE, team(20, true, Set.of()));
    check(r.ready(), "team should be ready");
    equal(CheckStatus.UNMET, status(r, RuleKey.PLAYER_PHOTO_REQUIRED), "photo warning");
    equal(100, r.progressPercent(), "progress");
  }

  public static void testUnpaidFeeBlocksSeasonStart() {
    TeamReadiness r = ENGINE.evaluate(LEAGUE, team(20, false, Set.of()));
    check(!r.ready(), "unpaid fee must block");
    equal(75, r.progressPercent(), "3 of 4 blocking checks pass");
  }

  public static void testWaiverOverridesUnmetRule() {
    TeamReadiness r = ENGINE.evaluate(LEAGUE, team(12, true, Set.of(RuleKey.TEAM_MIN_REGISTERED_CONTRACTS)));
    equal(CheckStatus.WAIVED, status(r, RuleKey.TEAM_MIN_REGISTERED_CONTRACTS), "waived");
    check(r.ready(), "waived rule should not block");
  }

  public static void testPlayerRulesAreNotApplicableToEmptySquad() {
    TeamReadiness r = ENGINE.evaluate(LEAGUE, team(0, true, Set.of()));
    equal(CheckStatus.NOT_APPLICABLE, status(r, RuleKey.PLAYER_DOCUMENT_REQUIRED), "documents");
    check(!r.ready(), "empty squad still fails the contract minimum");
  }

  public static void testSeasonBindingOverridesDivisionAndLeague() {
    Binding youthDivision = new Binding(
        List.of(new Rule(RuleKey.TEAM_MIN_REGISTERED_CONTRACTS, Severity.BLOCKING, Map.of("min", 14))),
        Set.of(RuleKey.TEAM_PARTICIPATION_FEE_PAID));
    Binding season = new Binding(
        List.of(new Rule(RuleKey.TEAM_MIN_REGISTERED_CONTRACTS, Severity.BLOCKING, Map.of("min", 12))),
        Set.of());

    List<Rule> effective = ReadinessEngine.resolve(LEAGUE, youthDivision, season);
    Rule min = effective.stream().filter(r -> r.key() == RuleKey.TEAM_MIN_REGISTERED_CONTRACTS).findFirst().orElseThrow();
    equal(12, min.params().get("min"), "most specific layer wins");
    check(effective.stream().noneMatch(r -> r.key() == RuleKey.TEAM_PARTICIPATION_FEE_PAID), "division disabled the fee rule");
    check(ENGINE.evaluate(effective, team(12, false, Set.of())).ready(), "12 players, no fee rule: ready");
  }

  public static void testInvalidParamsRejectedAtSaveTime() {
    throwsIllegalArgument(() -> ENGINE.validate(List.of(
        new Rule(RuleKey.TEAM_MAX_SQUAD_SIZE, Severity.BLOCKING, Map.of("max", 0)))), "zero squad cap");
    throwsIllegalArgument(() -> ENGINE.validate(List.of(
        new Rule(RuleKey.PLAYER_DOCUMENT_REQUIRED, Severity.BLOCKING, Map.of("documentTypes", List.of())))), "no documents");
    ENGINE.validate(LEAGUE);
  }

  private ReadinessEngineTest() {}
}
