// Centralised, hierarchical query keys. Prefix matching lets one invalidation
// call refresh every view derived from a season.
export const queryKeys = {
  season: (seasonId: string) => ['season', seasonId] as const,
  fixtures: (seasonId: string) => ['season', seasonId, 'fixtures'] as const,
  standings: (seasonId: string) => ['season', seasonId, 'standings'] as const,
  seasonReadiness: (seasonId: string) => ['season', seasonId, 'readiness'] as const,
  matchWeekList: (seasonId: string) => ['season', seasonId, 'match-weeks'] as const,
  matchWeekReadiness: (seasonId: string, week: number) => ['season', seasonId, 'match-weeks', week] as const,
  fixtureReadiness: (fixtureId: string) => ['fixture', fixtureId, 'readiness'] as const,
  playerStats: (seasonId: string) => ['season', seasonId, 'player-stats'] as const,
  scoutRankings: (seasonId: string) => ['season', seasonId, 'scouting'] as const,
} as const;
