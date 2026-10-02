package showcase.competition;

import static showcase.TestRunner.check;
import static showcase.TestRunner.equal;
import static showcase.TestRunner.throwsIllegalArgument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import showcase.competition.RoundRobinScheduler.Format;
import showcase.competition.RoundRobinScheduler.Pairing;

public final class RoundRobinSchedulerTest {

  private static List<String> teams(int n) {
    return IntStream.rangeClosed(1, n).mapToObj(i -> "T" + i).toList();
  }

  public static void testEveryPairMeetsExactlyOncePerLeg() {
    for (int n = 2; n <= 20; n++) {
      List<Pairing<String>> fixtures = RoundRobinScheduler.generate(teams(n), Format.SINGLE);
      Set<Set<String>> pairs = new HashSet<>();
      for (Pairing<String> p : fixtures) {
        check(pairs.add(Set.of(p.home(), p.away())), "duplicate pairing for n=" + n + ": " + p);
      }
      equal(n * (n - 1) / 2, pairs.size(), "distinct pairings for n=" + n);
    }
  }

  public static void testNoTeamPlaysTwiceInAMatchWeek() {
    for (int n = 2; n <= 20; n++) {
      Map<Integer, Set<String>> byWeek = new HashMap<>();
      for (Pairing<String> p : RoundRobinScheduler.generate(teams(n), Format.DOUBLE)) {
        Set<String> week = byWeek.computeIfAbsent(p.matchWeek(), w -> new HashSet<>());
        check(week.add(p.home()) && week.add(p.away()), "team double-booked in week " + p.matchWeek() + " (n=" + n + ")");
      }
    }
  }

  public static void testMatchWeekCountMatchesEstimate() {
    for (int n = 2; n <= 20; n++) {
      for (Format f : Format.values()) {
        int maxWeek = RoundRobinScheduler.generate(teams(n), f).stream().mapToInt(Pairing::matchWeek).max().orElse(0);
        equal(RoundRobinScheduler.matchWeeks(n, f), maxWeek, "weeks for n=" + n + " " + f);
      }
    }
  }

  public static void testOddTeamCountGivesEachTeamOneByePerLeg() {
    List<String> seven = teams(7);
    List<Pairing<String>> fixtures = RoundRobinScheduler.generate(seven, Format.SINGLE);
    for (String team : seven) {
      long played = fixtures.stream().filter(p -> p.home().equals(team) || p.away().equals(team)).count();
      equal(6L, played, "matches for " + team);
    }
  }

  public static void testSecondLegMirrorsFirstWithVenuesSwapped() {
    List<Pairing<String>> season = RoundRobinScheduler.generate(teams(6), Format.DOUBLE);
    int half = season.size() / 2;
    for (int i = 0; i < half; i++) {
      Pairing<String> first = season.get(i);
      Pairing<String> second = season.get(i + half);
      equal(first.home(), second.away(), "mirrored home");
      equal(first.matchWeek() + 5, second.matchWeek(), "mirrored week");
    }
  }

  public static void testHomeAndAwayAreBalanced() {
    for (int n = 2; n <= 20; n++) {
      Map<String, Integer> balance = new HashMap<>();
      for (Pairing<String> p : RoundRobinScheduler.generate(teams(n), Format.SINGLE)) {
        balance.merge(p.home(), 1, Integer::sum);
        balance.merge(p.away(), -1, Integer::sum);
      }
      int worst = balance.values().stream().mapToInt(Math::abs).max().orElse(0);
      check(worst <= 2, "home/away imbalance " + worst + " for n=" + n);
    }
  }

  public static void testRejectsInvalidInput() {
    throwsIllegalArgument(() -> RoundRobinScheduler.generate(List.of("solo"), Format.SINGLE), "single team");
    throwsIllegalArgument(() -> RoundRobinScheduler.generate(List.of("A", "A"), Format.SINGLE), "duplicate teams");
  }

  private RoundRobinSchedulerTest() {}
}
