// Shared domain contracts. In Flame these live in a workspace package consumed by
// both the web and mobile apps, and mirror the API's response DTOs.

export type UserRole = 'super_admin' | 'league_admin' | 'team_manager' | 'referee' | 'user';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export type SeasonStatus = 'DRAFT' | 'FIXTURES_GENERATED' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED';

export interface Season {
  id: string;
  divisionId: string;
  name: string;
  seasonYear: number;
  status: SeasonStatus;
  startDate: string;
  endDate: string;
}

export type FixtureStatus = 'SCHEDULED' | 'PLAYED' | 'POSTPONED' | 'CANCELLED';

export interface Fixture {
  id: string;
  seasonId: string;
  matchWeek: number;
  homeTeamId: string;
  awayTeamId: string;
  scheduledDate: string | null;
  scheduledTime: string | null;
  status: FixtureStatus;
}

export interface LineupSubmission {
  fixtureId: string;
  teamId: string;
  startingEleven: string[];
  substitutes: string[];
}
