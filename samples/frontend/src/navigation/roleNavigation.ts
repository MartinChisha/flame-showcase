import type { UserRole } from '../types/domain';

// Navigation is configuration, shared by the web sidebar and the mobile tab bar.
// Adding a role-specific feature is one entry here; both platforms pick it up.

export interface NavItem {
  id: string;
  label: string;
  /** Web path; mobile maps the same id to a screen name. */
  path: string;
  icon: string;
}

export type Capability = 'branchAdmin' | 'motherBodyAdmin' | 'storeOwner';

const COMMON_NAV: readonly NavItem[] = [
  { id: 'dashboard', label: 'Dashboard', path: '/app/dashboard', icon: 'layout-dashboard' },
  { id: 'standings', label: 'Standings', path: '/app/standings', icon: 'trophy' },
  { id: 'marketplace', label: 'Marketplace', path: '/app/marketplace', icon: 'store' },
];

const ROLE_NAV: Record<UserRole, readonly NavItem[]> = {
  super_admin: [
    { id: 'users', label: 'Users', path: '/app/admin/users', icon: 'users' },
    { id: 'leagues', label: 'Leagues', path: '/app/leagues', icon: 'landmark' },
    { id: 'referee-bodies', label: 'Referee bodies', path: '/app/referees/branches', icon: 'flag' },
  ],
  league_admin: [
    { id: 'my-league', label: 'My League', path: '/app/my-league', icon: 'landmark' },
    { id: 'approvals', label: 'Approvals', path: '/app/admin/contracts/pending', icon: 'file-check' },
  ],
  team_manager: [
    { id: 'my-team', label: 'My Team', path: '/app/my-team', icon: 'shield' },
    { id: 'lineups', label: 'Lineups', path: '/app/my-team/lineups', icon: 'layout-grid' },
  ],
  referee: [{ id: 'appointments', label: 'My appointments', path: '/app/referees/my-appointments', icon: 'calendar' }],
  user: [{ id: 'scouting', label: 'Scouting', path: '/app/scouting', icon: 'search' }],
};

// Sub-roles come from assignment records, not from the role enum.
const CAPABILITY_NAV: Record<Capability, NavItem> = {
  branchAdmin: { id: 'branch-admin', label: 'Branch admin', path: '/app/referees/branch-admin', icon: 'building' },
  motherBodyAdmin: { id: 'mother-body', label: 'Mother body', path: '/app/referees/mother-body', icon: 'network' },
  storeOwner: { id: 'my-store', label: 'My store', path: '/app/marketplace/my-store', icon: 'shopping-bag' },
};

export function navigationFor(role: UserRole, capabilities: ReadonlySet<Capability> = new Set()): NavItem[] {
  const items = [...COMMON_NAV.slice(0, 1), ...ROLE_NAV[role]];
  for (const capability of capabilities) items.push(CAPABILITY_NAV[capability]);
  items.push(...COMMON_NAV.slice(1));
  return items;
}
