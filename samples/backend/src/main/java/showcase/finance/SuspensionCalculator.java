package showcase.finance;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns card events into suspensions using a season's effective discipline rules.
 *
 * <p>Product rules: a suspension <em>includes the match in which it was earned</em>; a second yellow
 * in one match is treated as a red when the rule is enabled; reaching the season yellow threshold
 * triggers an accumulation ban that starts at the next fixture.
 */
public final class SuspensionCalculator {

  public enum Card { YELLOW, RED }

  public enum SanctionType { RED_CARD, YELLOW_ACCUMULATION }

  public record DisciplineRules(int redCardMatches, int yellowThreshold, int accumulationMatches, boolean doubleYellowIsRed) {
    public DisciplineRules {
      if (redCardMatches < 1 || accumulationMatches < 1 || yellowThreshold < 1) {
        throw new IllegalArgumentException("discipline parameters must be at least 1");
      }
    }
  }

  /**
   * @param coveredFixtureIds fixtures the player may not appear in, in order
   * @param sourceRef idempotency key, so reprocessing a report never duplicates a sanction
   */
  public record Sanction(SanctionType type, List<String> coveredFixtureIds, String sourceRef) {}

  public record Outcome(int newSeasonYellowTally, List<Sanction> sanctions) {}

  private final DisciplineRules rules;

  public SuspensionCalculator(DisciplineRules rules) {
    this.rules = rules;
  }

  /**
   * @param cardsThisMatch the player's cards in the source fixture, in minute order
   * @param seasonYellowTally yellows already counted this season before this match
   * @param currentFixtureId the fixture the cards were shown in
   * @param upcomingFixtureIds the team's remaining fixtures after the current one, in order
   */
  public Outcome process(
      List<Card> cardsThisMatch, int seasonYellowTally, String currentFixtureId, List<String> upcomingFixtureIds) {
    long yellows = cardsThisMatch.stream().filter(c -> c == Card.YELLOW).count();
    boolean straightRed = cardsThisMatch.contains(Card.RED);
    boolean doubleYellow = rules.doubleYellowIsRed() && yellows >= 2;

    if (straightRed || doubleYellow) {
      // A dismissal is sanctioned as a red; its yellows do not also count toward accumulation.
      List<String> covered = window(currentFixtureId, upcomingFixtureIds, rules.redCardMatches(), true);
      return new Outcome(seasonYellowTally,
          List.of(new Sanction(SanctionType.RED_CARD, covered, currentFixtureId + ":red")));
    }

    int tally = seasonYellowTally + (int) yellows;
    boolean crossedThreshold = seasonYellowTally < rules.yellowThreshold() && tally >= rules.yellowThreshold();
    if (!crossedThreshold) return new Outcome(tally, List.of());

    List<String> covered = window(currentFixtureId, upcomingFixtureIds, rules.accumulationMatches(), false);
    return new Outcome(0, // tally resets once the ban is issued
        List.of(new Sanction(SanctionType.YELLOW_ACCUMULATION, covered, currentFixtureId + ":yellows")));
  }

  private static List<String> window(String current, List<String> upcoming, int matches, boolean includeCurrent) {
    int fromUpcoming = includeCurrent ? matches - 1 : matches;
    List<String> next = upcoming.subList(0, Math.min(fromUpcoming, upcoming.size()));
    if (!includeCurrent) return List.copyOf(next);
    List<String> all = new ArrayList<>(next.size() + 1);
    all.add(current);
    all.addAll(next);
    return List.copyOf(all);
  }
}
