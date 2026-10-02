package showcase.match;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Decides whether a team's lineup can still be edited. Pure and clock-injected, so the scheduler,
 * the save endpoint and the submit endpoint all share one definition of "locked".
 */
public final class LineupLockPolicy {

  public enum LineupStatus { DRAFT, SUBMITTED, LOCKED }

  public enum LockReason { ALREADY_LOCKED, FIXTURE_PLAYED, KICKOFF_PASSED, DEADLINE_REACHED }

  public record Lineup(LineupStatus status) {}

  public record FixtureTiming(Instant kickoff, boolean played) {}

  public sealed interface Decision permits Editable, Locked {
    /** True when the caller should persist {@link LineupStatus#LOCKED}. */
    default boolean shouldPersistLock() {
      return this instanceof Locked locked && locked.reason() != LockReason.ALREADY_LOCKED;
    }
  }

  public record Editable() implements Decision {}

  public record Locked(LockReason reason) implements Decision {}

  private final Duration deadlineBeforeKickoff;

  /** @param deadlineBeforeKickoff configured per season, e.g. one hour */
  public LineupLockPolicy(Duration deadlineBeforeKickoff) {
    if (deadlineBeforeKickoff.isNegative()) throw new IllegalArgumentException("deadline must not be negative");
    this.deadlineBeforeKickoff = Objects.requireNonNull(deadlineBeforeKickoff);
  }

  public Decision evaluate(Lineup lineup, FixtureTiming fixture, Instant now) {
    if (lineup.status() == LineupStatus.LOCKED) return new Locked(LockReason.ALREADY_LOCKED);
    if (fixture.played()) return new Locked(LockReason.FIXTURE_PLAYED);
    if (fixture.kickoff() != null && !now.isBefore(fixture.kickoff())) return new Locked(LockReason.KICKOFF_PASSED);

    // A submitted lineup freezes at the deadline; a draft stays editable until kick-off
    // so a late team can still field a side.
    boolean submitted = lineup.status() == LineupStatus.SUBMITTED;
    if (submitted && fixture.kickoff() != null
        && !now.isBefore(fixture.kickoff().minus(deadlineBeforeKickoff))) {
      return new Locked(LockReason.DEADLINE_REACHED);
    }
    return new Editable();
  }
}
