package showcase.finance;

import static showcase.TestRunner.check;
import static showcase.TestRunner.equal;

import java.util.List;
import showcase.finance.SuspensionCalculator.Card;
import showcase.finance.SuspensionCalculator.DisciplineRules;
import showcase.finance.SuspensionCalculator.Outcome;
import showcase.finance.SuspensionCalculator.SanctionType;

public final class SuspensionCalculatorTest {

  private static final List<String> NEXT = List.of("F8", "F9", "F10", "F11");
  private static final SuspensionCalculator CALC =
      new SuspensionCalculator(new DisciplineRules(2, 5, 1, true));

  public static void testRedCardBanIncludesCurrentMatch() {
    Outcome o = CALC.process(List.of(Card.RED), 0, "F7", NEXT);
    equal(SanctionType.RED_CARD, o.sanctions().get(0).type(), "type");
    equal(List.of("F7", "F8"), o.sanctions().get(0).coveredFixtureIds(), "current match consumes one of two");
  }

  public static void testDoubleYellowIsTreatedAsRed() {
    Outcome o = CALC.process(List.of(Card.YELLOW, Card.YELLOW), 1, "F7", NEXT);
    equal(SanctionType.RED_CARD, o.sanctions().get(0).type(), "double yellow");
    equal(1, o.newSeasonYellowTally(), "dismissal yellows don't accumulate");
  }

  public static void testYellowAccumulationStartsNextFixture() {
    Outcome o = CALC.process(List.of(Card.YELLOW), 4, "F7", NEXT);
    equal(SanctionType.YELLOW_ACCUMULATION, o.sanctions().get(0).type(), "threshold reached");
    equal(List.of("F8"), o.sanctions().get(0).coveredFixtureIds(), "one-match ban next fixture");
    equal(0, o.newSeasonYellowTally(), "tally resets");
  }

  public static void testBelowThresholdOnlyIncrementsTally() {
    Outcome o = CALC.process(List.of(Card.YELLOW), 2, "F7", NEXT);
    check(o.sanctions().isEmpty(), "no sanction");
    equal(3, o.newSeasonYellowTally(), "tally");
  }

  public static void testBanTruncatedAtEndOfSeason() {
    Outcome o = new SuspensionCalculator(new DisciplineRules(4, 5, 1, true)).process(List.of(Card.RED), 0, "F7", List.of("F8"));
    equal(List.of("F7", "F8"), o.sanctions().get(0).coveredFixtureIds(), "only remaining fixtures covered");
  }

  public static void testSourceRefIsDeterministicForIdempotency() {
    String a = CALC.process(List.of(Card.RED), 0, "F7", NEXT).sanctions().get(0).sourceRef();
    String b = CALC.process(List.of(Card.RED), 0, "F7", NEXT).sanctions().get(0).sourceRef();
    equal(a, b, "same event, same key");
  }

  private SuspensionCalculatorTest() {}
}
