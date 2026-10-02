import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query';
import { competitionApi, matchApi } from '../api/competitionApi';
import type { Fixture, LineupSubmission, Season } from '../types/domain';
import { queryKeys } from './queryKeys';

// These hooks are shared by the web and mobile apps. Screens never call the
// transport directly, so caching and invalidation rules live in one place.

export function useSeason(seasonId: string | undefined) {
  return useQuery<Season>({
    queryKey: queryKeys.season(seasonId ?? ''),
    queryFn: () => competitionApi.season(seasonId as string),
    enabled: Boolean(seasonId),
  });
}

export function useFixturesBySeason(seasonId: string | undefined) {
  return useQuery<Fixture[]>({
    queryKey: queryKeys.fixtures(seasonId ?? ''),
    queryFn: () => competitionApi.fixturesBySeason(seasonId as string),
    enabled: Boolean(seasonId),
    select: (fixtures) => [...fixtures].sort((a, b) => a.matchWeek - b.matchWeek),
  });
}

/** Group fixtures into match weeks for the season calendar. */
export function groupByMatchWeek(fixtures: readonly Fixture[]): Map<number, Fixture[]> {
  const weeks = new Map<number, Fixture[]>();
  for (const fixture of fixtures) {
    const bucket = weeks.get(fixture.matchWeek) ?? [];
    bucket.push(fixture);
    weeks.set(fixture.matchWeek, bucket);
  }
  return weeks;
}

/**
 * Readiness is derived from lineups, referees and schedules, so anything that
 * changes those inputs must refresh every readiness view, on every platform.
 */
export function invalidateMatchWeekViews(queryClient: QueryClient, seasonId: string, fixtureId?: string) {
  void queryClient.invalidateQueries({ queryKey: queryKeys.matchWeekList(seasonId) });
  if (fixtureId) {
    void queryClient.invalidateQueries({ queryKey: queryKeys.fixtureReadiness(fixtureId) });
  }
}

export function useSubmitLineup(seasonId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (lineup: LineupSubmission) => matchApi.submitLineup(lineup),
    onSuccess: (_void, lineup) => invalidateMatchWeekViews(queryClient, seasonId, lineup.fixtureId),
  });
}

/** A submitted match report fans out to standings, stats, scouting and readiness. */
export function invalidateAfterMatchReport(queryClient: QueryClient, seasonId: string, fixtureId: string) {
  invalidateMatchWeekViews(queryClient, seasonId, fixtureId);
  for (const key of [
    queryKeys.standings(seasonId),
    queryKeys.playerStats(seasonId),
    queryKeys.scoutRankings(seasonId),
    queryKeys.seasonReadiness(seasonId),
  ]) {
    void queryClient.invalidateQueries({ queryKey: key });
  }
}
