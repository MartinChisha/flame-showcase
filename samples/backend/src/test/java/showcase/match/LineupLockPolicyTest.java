package showcase.match;

import static showcase.TestRunner.check;
import static showcase.TestRunner.equal;

import java.time.Duration;
import java.time.Instant;
import showcase.match.LineupLockPolicy.Decision;
import showcase.match.LineupLockPolicy.Editable;
import showcase.match.LineupLockPolicy.FixtureTiming;
import showcase.match.LineupLockPolicy.Lineup;
import showcase.match.LineupLockPolicy.LineupStatus;
import showcase.match.LineupLockPolicy.LockReason;
import showcase.match.LineupLockPolicy.Locked;

public final class LineupLockPolicyTest {

  private static final Instant KICKOFF = Instant.parse("2027-03-06T15:00:00Z");
  private static final LineupLockPolicy POLICY = new LineupLockPolicy(Duration.ofHours(1));
  private static final FixtureTiming UPCOMING = new FixtureTiming(KICKOFF, false);

  private static LockReason reason(Decision d) {
    return d instanceof Locked locked ? locked.reason() : null;
  }

  public static void testDraftEditableUntilKickoff() {
    Decision d = POLICY.evaluate(new Lineup(LineupStatus.DRAFT), UPCOMING, KICKOFF.minusSeconds(60));
    check(d instanceof Editable, "late draft is still editable");
  }

  public static void testSubmittedLineupLocksAtDeadline() {
    Decision before = POLICY.evaluate(new Lineup(LineupStatus.SUBMITTED), UPCOMING, KICKOFF.minus(Duration.ofMinutes(61)));
    Decision at = POLICY.evaluate(new Lineup(LineupStatus.SUBMITTED), UPCOMING, KICKOFF.minus(Duration.ofHours(1)));
    check(before instanceof Editable, "editable before deadline");
    equal(LockReason.DEADLINE_REACHED, reason(at), "locked at deadline");
    check(at.shouldPersistLock(), "first lock should be persisted");
  }

  public static void testKickoffAndPlayedFixturesLockEverything() {
    equal(LockReason.KICKOFF_PASSED, reason(POLICY.evaluate(new Lineup(LineupStatus.DRAFT), UPCOMING, KICKOFF)), "kickoff");
    equal(LockReason.FIXTURE_PLAYED,
        reason(POLICY.evaluate(new Lineup(LineupStatus.DRAFT), new FixtureTiming(KICKOFF, true), KICKOFF.minusSeconds(7200))),
        "played");
  }

  public static void testAlreadyLockedIsNotPersistedAgain() {
    Decision d = POLICY.evaluate(new Lineup(LineupStatus.LOCKED), UPCOMING, KICKOFF.minusSeconds(86_400));
    equal(LockReason.ALREADY_LOCKED, reason(d), "already locked");
    check(!d.shouldPersistLock(), "no redundant write");
  }

  public static void testUnscheduledFixtureNeverTimeLocks() {
    Decision d = POLICY.evaluate(new Lineup(LineupStatus.SUBMITTED), new FixtureTiming(null, false), KICKOFF);
    check(d instanceof Editable, "no kickoff yet");
  }

  private LineupLockPolicyTest() {}
}
