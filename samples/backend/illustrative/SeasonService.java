// ILLUSTRATIVE ONLY: shows service-layer scoping and cross-context gates. Not compiled.
package showcase.competition.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SeasonService {

  private final SeasonRepository seasons;
  private final FixtureRepository fixtures;
  private final TeamQueries teams;
  private final LeagueAccess leagueAccess;          // league module
  private final PolicyReadinessService readiness;   // policy module

  SeasonService(SeasonRepository seasons, FixtureRepository fixtures, TeamQueries teams,
      LeagueAccess leagueAccess, PolicyReadinessService readiness) {
    this.seasons = seasons;
    this.fixtures = fixtures;
    this.teams = teams;
    this.leagueAccess = leagueAccess;
    this.readiness = readiness;
  }

  @Transactional
  public FixtureGenerationResponse generateFixtures(UUID seasonId, RoundRobinScheduler.Format format, FlameUser actor) {
    Season season = seasons.findById(seasonId).orElseThrow(() -> new NotFoundException("Season not found"));

    // Role was checked by @PreAuthorize; scope is checked here.
    leagueAccess.requireAdminOfDivision(actor, season.getDivisionId());

    if (season.getStatus().isAfter(SeasonStatus.FIXTURES_GENERATED)) {
      throw new ConflictException("Fixtures cannot be regenerated once the season has started");
    }

    var activeTeams = teams.activeTeamIdsInDivision(season.getDivisionId());
    if (activeTeams.size() < 2) {
      throw new UnprocessableException("At least two active teams are required");
    }

    fixtures.deleteBySeasonId(seasonId);
    var pairings = RoundRobinScheduler.generate(activeTeams, format);
    fixtures.saveAll(pairings.stream().map(p -> Fixture.scheduled(seasonId, p)).toList());

    season.markFixturesGenerated(format);
    return FixtureGenerationResponse.of(season, pairings.size(), RoundRobinScheduler.matchWeeks(activeTeams.size(), format));
  }

  @Transactional
  public SeasonResponse changeStatus(UUID seasonId, SeasonStatus target, FlameUser actor) {
    Season season = seasons.findById(seasonId).orElseThrow(() -> new NotFoundException("Season not found"));
    leagueAccess.requireAdminOfDivision(actor, season.getDivisionId());

    if (target == SeasonStatus.IN_PROGRESS) {
      var summary = readiness.recalculate(seasonId);
      if (!summary.ready()) {
        throw new SeasonNotReadyException(summary.blockingIssues()); // 422 with per-team reasons
      }
    }
    season.transitionTo(target); // enforces the allowed lifecycle transitions
    return SeasonResponse.from(season);
  }
}
