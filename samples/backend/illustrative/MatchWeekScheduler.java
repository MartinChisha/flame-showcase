// ILLUSTRATIVE ONLY: shows scheduled-job conventions. Not compiled (needs Spring).
package showcase.operations.scheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Operational reminders. Every job:
 * <ul>
 *   <li>only considers seasons that are IN_PROGRESS,</li>
 *   <li>evaluates deadlines relative to each season's own schedule settings,</li>
 *   <li>delivers through {@code deliverIfNew}, which records (type, fixture, recipient) in a
 *       delivery log so a job that runs every 15 minutes never sends the same reminder twice.</li>
 * </ul>
 */
@Component
class MatchWeekScheduler {

  private final FixtureQueries fixtures;
  private final NotificationService notifications;
  private final NotificationRecipientResolver recipients;
  private final Clock clock;

  MatchWeekScheduler(FixtureQueries fixtures, NotificationService notifications,
      NotificationRecipientResolver recipients, Clock clock) {
    this.fixtures = fixtures;
    this.notifications = notifications;
    this.recipients = recipients;
    this.clock = clock;
  }

  /** Lineup reminders: 24 hours out, and again at the lock deadline. */
  @Scheduled(cron = "0 */15 * * * *")
  void scanUpcomingFixtures() {
    Instant now = clock.instant();
    for (var f : fixtures.upcomingInActiveSeasons(now, Duration.ofHours(25))) {
      for (var team : f.teamsWithoutSubmittedLineup()) {
        var type = f.kickoff().minus(f.lineupDeadline()).isAfter(now.plus(Duration.ofHours(1)))
            ? NotificationType.LINEUP_REMINDER_24H
            : NotificationType.LINEUP_REMINDER_1H;
        recipients.teamManagers(team).forEach(user -> notifications.deliverIfNew(type, f.id(), user));
      }
    }
  }

  /** Branch admins: fixtures inside the appointment window with no main referee. */
  @Scheduled(cron = "0 0 8 * * *")
  void scanRefereeGaps() {
    for (var f : fixtures.missingMainRefereeWithinAssignWindow(clock.instant())) {
      recipients.branchAdminsServing(f).forEach(user ->
          notifications.deliverIfNew(NotificationType.REFEREE_ASSIGNMENT_DUE, f.id(), user));
    }
  }

  /** League admins: next match week still undated. */
  @Scheduled(cron = "0 0 9 * * *")
  void scanNextWeekDates() { /* ... */ }

  /** Referees: report overdue after kick-off. */
  @Scheduled(cron = "0 */30 * * * *")
  void scanOverdueReports() { /* ... */ }
}
