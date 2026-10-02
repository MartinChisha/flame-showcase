// ILLUSTRATIVE ONLY: shows Flame's controller conventions. Not compiled (needs Spring).
package showcase.competition.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Conventions every module follows:
 * <ul>
 *   <li>Role checks at the method boundary with {@code @PreAuthorize}.</li>
 *   <li>Resource scope (is this user an admin of <em>this</em> league?) in the service.</li>
 *   <li>Request and response DTOs only; entities never cross the controller.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/seasons")
class SeasonController {

  private final SeasonService seasons;

  SeasonController(SeasonService seasons) {
    this.seasons = seasons;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('super_admin', 'league_admin')")
  SeasonResponse create(@Valid @RequestBody SeasonCreateRequest request, @AuthenticationPrincipal FlameUser actor) {
    return seasons.create(request, actor);
  }

  @GetMapping("/{seasonId}/fixtures")
  @PreAuthorize("hasAnyRole('super_admin', 'league_admin', 'team_manager', 'referee')")
  List<FixtureResponse> fixtures(@PathVariable UUID seasonId) {
    return seasons.fixtures(seasonId);
  }

  @PostMapping("/{seasonId}/fixtures/generate")
  @PreAuthorize("hasAnyRole('super_admin', 'league_admin')")
  FixtureGenerationResponse generate(
      @PathVariable UUID seasonId,
      @Valid @RequestBody FixtureGenerationRequest request,
      @AuthenticationPrincipal FlameUser actor) {
    return seasons.generateFixtures(seasonId, request.format(), actor);
  }

  @PutMapping("/{seasonId}/status")
  @PreAuthorize("hasAnyRole('super_admin', 'league_admin')")
  SeasonResponse changeStatus(
      @PathVariable UUID seasonId, @Valid @RequestBody SeasonStatusRequest request, @AuthenticationPrincipal FlameUser actor) {
    // Moving to IN_PROGRESS is rejected server-side unless policy readiness passes for every team.
    return seasons.changeStatus(seasonId, request.status(), actor);
  }
}
