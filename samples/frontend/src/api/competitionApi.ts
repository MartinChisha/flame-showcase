import { getApiClient } from './createApiClient';
import type { AuthResponse, Fixture, LineupSubmission, Season, User } from '../types/domain';

// One module per bounded context. Functions return typed payloads, never raw
// Axios responses, so hooks and screens are insulated from the transport.
// Paths below are illustrative.

export const authApi = {
  async exchangeIdentityToken(idToken: string): Promise<AuthResponse> {
    const { data } = await getApiClient().post<AuthResponse>('/auth/exchange', { idToken });
    return data;
  },
  async currentUser(): Promise<User> {
    const { data } = await getApiClient().get<User>('/users/me');
    return data;
  },
};

export const competitionApi = {
  async season(seasonId: string): Promise<Season> {
    const { data } = await getApiClient().get<Season>(`/seasons/${seasonId}`);
    return data;
  },
  async fixturesBySeason(seasonId: string): Promise<Fixture[]> {
    const { data } = await getApiClient().get<Fixture[]>(`/seasons/${seasonId}/fixtures`);
    return data;
  },
  async createSeason(input: Omit<Season, 'id' | 'status'>): Promise<Season> {
    const { data } = await getApiClient().post<Season>('/seasons', input);
    return data;
  },
};

export const matchApi = {
  async submitLineup(lineup: LineupSubmission): Promise<void> {
    await getApiClient().put(`/lineups/${lineup.fixtureId}/${lineup.teamId}/submit`, lineup);
  },
};
