package showcase.competition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Round-robin fixture generation using the circle method.
 *
 * <p>One team is held fixed while the others rotate one position per round. With an odd number of
 * teams a bye is inserted, so every team rests exactly once per leg. A double round-robin mirrors
 * the first leg with home and away swapped.
 */
public final class RoundRobinScheduler {

  public enum Format {
    SINGLE,
    DOUBLE
  }

  public record Pairing<T>(int matchWeek, T home, T away) {
    public Pairing {
      Objects.requireNonNull(home, "home");
      Objects.requireNonNull(away, "away");
      if (home.equals(away)) throw new IllegalArgumentException("a team cannot play itself");
    }
  }

  private RoundRobinScheduler() {}

  /** Number of match weeks a season will need, without generating it. */
  public static int matchWeeks(int teamCount, Format format) {
    if (teamCount < 2) return 0;
    int perLeg = teamCount % 2 == 0 ? teamCount - 1 : teamCount;
    return format == Format.DOUBLE ? perLeg * 2 : perLeg;
  }

  public static <T> List<Pairing<T>> generate(List<T> teams, Format format) {
    if (teams.size() < 2) {
      throw new IllegalArgumentException("at least two teams are required");
    }
    if (teams.stream().distinct().count() != teams.size()) {
      throw new IllegalArgumentException("teams must be unique");
    }

    List<T> ring = new ArrayList<>(teams);
    if (ring.size() % 2 == 1) ring.add(null); // bye slot

    int n = ring.size();
    int rounds = n - 1;
    List<Pairing<T>> firstLeg = new ArrayList<>(rounds * n / 2);

    for (int round = 0; round < rounds; round++) {
      for (int i = 0; i < n / 2; i++) {
        T a = ring.get(i);
        T b = ring.get(n - 1 - i);
        if (a == null || b == null) continue; // one team rests this week

        // Alternate venues so no team accumulates long home or away streaks:
        // the fixed team flips every round, the others flip by board position.
        boolean aHome = (i == 0) ? round % 2 == 0 : i % 2 == 1;
        firstLeg.add(aHome ? new Pairing<>(round + 1, a, b) : new Pairing<>(round + 1, b, a));
      }
      // Keep index 0 fixed and rotate the rest one step clockwise.
      Collections.rotate(ring.subList(1, n), 1);
    }

    if (format == Format.SINGLE) return List.copyOf(firstLeg);

    List<Pairing<T>> season = new ArrayList<>(firstLeg);
    for (Pairing<T> p : firstLeg) {
      season.add(new Pairing<>(p.matchWeek() + rounds, p.away(), p.home()));
    }
    return List.copyOf(season);
  }
}
